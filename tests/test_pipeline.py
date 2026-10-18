"""End-to-end and per-stage tests for the polyglot pipeline.

Run with:  python3 -m unittest discover -s tests -v
"""
import os
import shutil
import subprocess
import sys
import unittest
from pathlib import Path
from unittest import mock

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT))

from orchestrator import main as orchestrator  # noqa: E402
from orchestrator.main import PipelineError, Stage, exe, load_config, run_pipeline  # noqa: E402


def run(command, data=None):
    return subprocess.run(command, input=data, capture_output=True, text=True, timeout=120)


class EndToEndTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        # Builds everything once; the other tests reuse the binaries.
        run_pipeline("Hello, World!", quiet=True)

    def pipeline(self, message):
        return run_pipeline(message, quiet=True, rebuild=False)

    def test_hello_world(self):
        self.assertEqual(self.pipeline("Hello, World!"), "Hello, World!\n")

    def test_every_printable_ascii_character(self):
        message = "".join(chr(code) for code in range(32, 127))
        self.assertEqual(self.pipeline(message), message + "\n")

    def test_single_character(self):
        self.assertEqual(self.pipeline("X"), "X\n")

    def test_empty_message_is_rejected(self):
        with self.assertRaises(PipelineError):
            self.pipeline("")

    def test_non_ascii_message_is_rejected(self):
        with self.assertRaises(PipelineError):
            self.pipeline("Привіт")


class ConfigTests(unittest.TestCase):
    def test_json_is_the_source_of_truth(self):
        self.assertEqual(load_config()["message"], "Hello, World!")

    def test_other_configs_really_do_contradict(self):
        yaml_text = (ROOT / "config" / "pipeline.yaml").read_text(encoding="utf-8")
        toml_text = (ROOT / "config" / "pipeline.toml").read_text(encoding="utf-8")
        xml_text = (ROOT / "config" / "pipeline.xml").read_text(encoding="utf-8")
        self.assertIn("Hello World", yaml_text)
        self.assertIn("hello, world!", toml_text)
        self.assertIn("<message>Hi</message>", xml_text)


class PortabilityTests(unittest.TestCase):
    """The fallbacks must produce exactly the same result as the native stages."""

    def test_portable_mode_uses_c_printer_and_python_decoder(self):
        self.assertEqual(run_pipeline("Hello, World!", quiet=True, portable=True), "Hello, World!\n")

    def test_portable_mode_handles_every_printable_ascii_character(self):
        message = "".join(chr(code) for code in range(32, 127))
        self.assertEqual(run_pipeline(message, quiet=True, portable=True), message + "\n")

    def test_missing_perl_falls_back_to_python(self):
        real_which = shutil.which
        with mock.patch.object(orchestrator.shutil, "which",
                               side_effect=lambda tool: None if tool == "perl" else real_which(tool)):
            self.assertEqual(run_pipeline("Hi", quiet=True), "Hi\n")

    def test_failing_native_assembly_falls_back_to_c(self):
        broken = Stage(name="broken-asm", language="Assembly", requires=[], build=[["false"]],
                       run=[str(exe("printer"))], fallback=orchestrator.c_printer_fallback())
        with mock.patch.object(orchestrator, "native_asm_stage", return_value=broken):
            self.assertEqual(run_pipeline("Hello, World!", quiet=True), "Hello, World!\n")

    def test_every_platform_has_a_known_asm_variant_file(self):
        asm = ROOT / "src" / "asm"
        for name in ("printer_linux_x86_64.s", "printer_linux_arm64.s",
                     "printer_macos_x86_64.s", "printer_macos_arm64.s"):
            self.assertTrue((asm / name).is_file(), name)


class StageTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        run_pipeline("Hello, World!", quiet=True)

    def test_encoder_xors_with_compile_time_key(self):
        result = run([str(exe("encoder"))], "72 101\n")
        self.assertEqual(result.stdout.split("\n")[:2], ["KEY 200", "%d %d" % (72 ^ 200, 101 ^ 200)])

    def test_encoder_rejects_empty_input(self):
        self.assertNotEqual(run([str(exe("encoder"))], "").returncode, 0)

    def test_relay_preserves_data(self):
        data = "KEY 200\n128 173 164\n"
        result = run(["node", str(ROOT / "src" / "js" / "relay.js")], data)
        self.assertEqual(result.stdout, data)

    def test_relay_rejects_out_of_range_values(self):
        result = run(["node", str(ROOT / "src" / "js" / "relay.js")], "KEY 200\n999\n")
        self.assertNotEqual(result.returncode, 0)

    def test_decoder_uses_numeric_xor(self):
        result = run(["perl", str(ROOT / "src" / "perl" / "decoder.pl")], "KEY 200\n128 173\n")
        self.assertEqual(result.stdout.strip(), "72 101")

    def test_c_stage_assembles_string(self):
        self.assertEqual(run([str(exe("assembler"))], "72 105").stdout, "Hi")

    def test_c_stage_rejects_control_characters(self):
        self.assertEqual(run([str(exe("assembler"))], "72 7").returncode, 3)

    def test_assembly_prints_input_and_newline(self):
        self.assertEqual(run([str(exe("printer"))], "Hello, World!").stdout, "Hello, World!\n")

    def test_assembly_fails_on_empty_input(self):
        self.assertEqual(run([str(exe("printer"))], "").returncode, 1)

    def test_java_self_test_covers_all_providers(self):
        result = subprocess.run(
            ["java", str(ROOT / "src" / "java" / "HelloEnterprise.java"), "A"],
            capture_output=True, text=True, timeout=120,
            env={"HELLO_SELFTEST": "1", "HELLO_VERBOSE": "1", "PATH": os.environ["PATH"]},
        )
        self.assertEqual(result.returncode, 0)
        self.assertIn("self test passed for 95 providers", result.stderr)
        self.assertEqual(result.stdout.strip(), "65")


if __name__ == "__main__":
    unittest.main()
