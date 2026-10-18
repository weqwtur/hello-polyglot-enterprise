#!/usr/bin/env python3
"""Orchestrator for the Polyglot Enterprise Hello World.

Builds every stage with whatever toolchain is installed, then pushes the message
through the pipeline stage by stage:

    Java -> C++ -> Node.js -> Perl -> (Go) -> (Rust) -> C -> Assembly

Portability: the Assembly stage has one native variant per OS/CPU, and both it and the
Perl stage have fallbacks (portable C and Python). Stages marked optional are skipped
when their toolchain is missing or fails to build.
"""
import argparse
import json
import os
import platform
import shutil
import subprocess
import sys
import time
from dataclasses import dataclass, field
from pathlib import Path
from typing import List, Optional

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "src"
BUILD = ROOT / "build"
CONFIG = ROOT / "config" / "pipeline.json"


class PipelineError(RuntimeError):
    """Raised when a mandatory stage cannot be built or run."""


def exe(name: str) -> Path:
    """Path of a built binary, with .exe on Windows."""
    return BUILD / (name + (".exe" if os.name == "nt" else ""))


def host() -> tuple:
    """Returns (system, arch) normalized, e.g. ("Linux", "x86_64") or ("Darwin", "arm64")."""
    machine = platform.machine().lower()
    arch = {"amd64": "x86_64", "x86_64": "x86_64", "arm64": "arm64", "aarch64": "arm64"}.get(machine, machine)
    return platform.system(), arch


@dataclass
class Stage:
    name: str
    language: str
    requires: List[str]
    run: List[str]
    build: List[List[str]] = field(default_factory=list)
    build_cwd: Optional[Path] = None
    probe: Optional[List[str]] = None
    optional: bool = False
    fallback: Optional["Stage"] = None
    elapsed: float = 0.0


def c_printer_fallback() -> Stage:
    return Stage(
        name="c-printer-fallback",
        language="C",
        requires=["gcc"],
        build=[["gcc", "-std=c11", "-O2", "-Wall", "-Wextra", "-o", str(exe("printer")),
                str(SRC / "c" / "printer_fallback.c")]],
        run=[str(exe("printer"))],
    )


def native_asm_stage() -> Optional[Stage]:
    """Picks the assembly printer for this OS/CPU, or None if there is no variant."""
    system, arch = host()
    asm = SRC / "asm"
    if system == "Linux" and arch in ("x86_64", "arm64"):
        source = asm / ("printer_linux_%s.s" % arch)
        return Stage(
            name="asm-printer (linux-%s)" % arch,
            language="Assembly",
            requires=["as", "ld"],
            build=[["as", "-o", str(BUILD / "printer.o"), str(source)],
                   ["ld", "-o", str(exe("printer")), str(BUILD / "printer.o")]],
            run=[str(exe("printer"))],
        )
    if system == "Darwin" and arch in ("x86_64", "arm64"):
        source = asm / ("printer_macos_%s.s" % arch)
        return Stage(
            name="asm-printer (macos-%s)" % arch,
            language="Assembly",
            requires=["cc"],
            build=[["cc", "-o", str(exe("printer")), str(source)]],
            run=[str(exe("printer"))],
        )
    return None


def make_stages() -> List[Stage]:
    printer = native_asm_stage()
    fallback = c_printer_fallback()
    if printer is None:
        printer = fallback
    else:
        printer.fallback = fallback

    decoder = Stage(
        name="perl-decoder",
        language="Perl",
        requires=["perl"],
        run=["perl", str(SRC / "perl" / "decoder.pl")],
        fallback=Stage(
            name="python-decoder-fallback",
            language="Python",
            requires=[],
            run=[sys.executable, str(SRC / "python" / "decoder.py")],
        ),
    )

    return [
        Stage(
            name="java-enterprise",
            language="Java",
            requires=["java"],
            probe=["java", "-version"],
            run=["java", str(SRC / "java" / "HelloEnterprise.java"), "{message}"],
        ),
        Stage(
            name="cpp-encoder",
            language="C++",
            requires=["g++"],
            build=[["g++", "-std=c++17", "-O2", "-Wall", "-Wextra", "-o", str(exe("encoder")),
                    str(SRC / "cpp" / "encoder.cpp")]],
            run=[str(exe("encoder"))],
        ),
        Stage(
            name="node-relay",
            language="JavaScript",
            requires=["node"],
            run=["node", str(SRC / "js" / "relay.js")],
        ),
        decoder,
        Stage(
            name="go-checksum",
            language="Go",
            requires=["go"],
            build=[["go", "build", "-o", str(exe("checksum")), "."]],
            build_cwd=SRC / "go",
            run=[str(exe("checksum"))],
            optional=True,
        ),
        Stage(
            name="rust-validator",
            language="Rust",
            requires=["rustc"],
            build=[["rustc", "-O", "-o", str(exe("validator")), str(SRC / "rust" / "validator.rs")]],
            run=[str(exe("validator"))],
            optional=True,
        ),
        Stage(
            name="c-assembler",
            language="C",
            requires=["gcc"],
            build=[["gcc", "-std=c11", "-O2", "-Wall", "-Wextra", "-o", str(exe("assembler")),
                    str(SRC / "c" / "assembler.c"), str(SRC / "c" / "ascii_guard.c")]],
            run=[str(exe("assembler"))],
        ),
        printer,
    ]


def log(message: str, quiet: bool = False) -> None:
    if not quiet:
        print("[orchestrator] " + message, file=sys.stderr)


def chain_of(stage: Stage) -> List[Stage]:
    chain = []
    current: Optional[Stage] = stage
    while current is not None:
        chain.append(current)
        current = current.fallback
    return chain


def try_build(stage: Stage) -> Optional[str]:
    """Builds one candidate. Returns None on success or a description of the problem."""
    missing = [tool for tool in stage.requires if shutil.which(tool) is None]
    if missing:
        return "%s needs %s, which is not installed" % (stage.name, ", ".join(missing))
    if stage.probe is not None:
        try:
            probe = subprocess.run(stage.probe, capture_output=True, text=True, timeout=60)
        except (OSError, subprocess.TimeoutExpired) as error:
            return "%s is not usable: %s" % (stage.name, error)
        if probe.returncode != 0:
            return "%s is not usable (%s)" % (stage.name, (probe.stderr or probe.stdout).strip()[:200])
    for command in stage.build:
        result = subprocess.run(command, cwd=stage.build_cwd, capture_output=True, text=True)
        if result.returncode != 0:
            return "build failed for %s:\n%s" % (stage.name, result.stderr.strip())
    return None


def prepare(stage: Stage, quiet: bool, portable: bool = False) -> Optional[Stage]:
    """Returns the first usable variant of a stage, or None for a skippable optional stage."""
    chain = chain_of(stage)
    if portable and len(chain) > 1:
        chain = chain[-1:]  # force the most portable variant, used for testing the fallbacks
    last_error = ""
    for index, candidate in enumerate(chain):
        error = try_build(candidate)
        if error is None:
            suffix = "  (fallback)" if candidate is not stage else ""
            log("built %-26s [%s]%s" % (candidate.name, candidate.language, suffix), quiet)
            return candidate
        last_error = error
        if index + 1 < len(chain):
            log("%s\n[orchestrator] trying fallback..." % error, quiet)
    if stage.optional:
        log("skipping %s (%s): %s" % (stage.name, stage.language, last_error.splitlines()[0]), quiet)
        return None
    raise PipelineError(last_error)


def select_prebuilt(stage: Stage) -> Optional[Stage]:
    """For --no-build: first variant whose tools and binary already exist."""
    for candidate in chain_of(stage):
        if any(shutil.which(tool) is None for tool in candidate.requires):
            continue
        if candidate.build and not Path(candidate.run[0]).exists():
            continue
        return candidate
    if stage.optional:
        return None
    raise PipelineError("%s has no usable prebuilt variant; run without --no-build" % stage.name)


def run_stage(stage: Stage, data: Optional[str], message: str, verbose: bool, timeout: int,
              quiet: bool = False) -> str:
    command = [part.replace("{message}", message) for part in stage.run]
    env = dict(os.environ, HELLO_VERBOSE="1") if verbose else None
    started = time.perf_counter()
    result = subprocess.run(command, input=data, capture_output=True, text=True, timeout=timeout, env=env)
    stage.elapsed = time.perf_counter() - started
    if result.stderr.strip():
        for line in result.stderr.strip().splitlines():
            log("  " + line, quiet)
    if result.returncode != 0:
        raise PipelineError("stage %s failed with exit code %d" % (stage.name, result.returncode))
    return result.stdout


def load_config() -> dict:
    with open(CONFIG, "r", encoding="utf-8") as handle:
        return json.load(handle)


def run_pipeline(message: str, verbose: bool = False, quiet: bool = False, rebuild: bool = True,
                 timeout: int = 120, portable: bool = False) -> str:
    BUILD.mkdir(exist_ok=True)
    selected: List[Stage] = []
    for stage in make_stages():
        chosen = prepare(stage, quiet, portable) if rebuild else select_prebuilt(stage)
        if chosen is not None:
            selected.append(chosen)

    data: Optional[str] = None
    for stage in selected:
        data = run_stage(stage, data, message, verbose, timeout, quiet)
        log("%-26s ok in %.3fs" % (stage.name, stage.elapsed), quiet)
    if data is None:
        raise PipelineError("pipeline produced no output")
    return data


def main(argv: Optional[List[str]] = None) -> int:
    parser = argparse.ArgumentParser(description="Polyglot Enterprise Hello World")
    parser.add_argument("--message", help="message to print (default: from config/pipeline.json)")
    parser.add_argument("--verbose", action="store_true", help="show logs from every stage")
    parser.add_argument("--quiet", action="store_true", help="print only the final output")
    parser.add_argument("--no-build", action="store_true", help="reuse binaries from build/")
    parser.add_argument("--build-only", action="store_true", help="compile every stage and exit")
    parser.add_argument("--portable", action="store_true",
                        help="use the portable fallbacks (C printer, Python decoder) instead of Assembly and Perl")
    parser.add_argument("--info", action="store_true", help="print detected platform and exit")
    args = parser.parse_args(argv)

    if args.info:
        system, arch = host()
        native = native_asm_stage()
        print("platform: %s %s" % (system, arch))
        print("assembly: %s" % (native.name if native else "no native variant, will use portable C"))
        return 0

    portable = args.portable or os.environ.get("HELLO_PORTABLE") == "1"

    if args.build_only:
        BUILD.mkdir(exist_ok=True)
        try:
            for stage in make_stages():
                prepare(stage, args.quiet, portable)
        except PipelineError as error:
            print("[orchestrator] FAILED: %s" % error, file=sys.stderr)
            return 1
        return 0

    config = load_config()
    message = args.message if args.message is not None else config["message"]
    try:
        output = run_pipeline(message, verbose=args.verbose, quiet=args.quiet, rebuild=not args.no_build,
                              timeout=int(config.get("timeout_seconds", 120)), portable=portable)
    except PipelineError as error:
        print("[orchestrator] FAILED: %s" % error, file=sys.stderr)
        return 1
    except subprocess.TimeoutExpired as error:
        print("[orchestrator] FAILED: timeout in %s" % error.cmd, file=sys.stderr)
        return 2

    sys.stdout.write(output)
    if output.rstrip("\r\n") != message:
        print("[orchestrator] WARNING: output differs from input message", file=sys.stderr)
        return 3
    return 0


if __name__ == "__main__":
    sys.exit(main())
