#!/usr/bin/env python3
"""Generates src/cpp/generated_asserts.hpp: one compile-time round-trip
assertion for every byte value."""
import os

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "cpp", "generated_asserts.hpp")


def main():
    lines = ["// GENERATED FILE. Run tools/gen_cpp.py instead of editing.",
             "#pragma once",
             '#include "encoder_core.hpp"',
             "",
             "namespace hello::asserts {",
             ""]
    for value in range(256):
        lines.append("static_assert(hello::core::decode(hello::core::encode(%d, hello::core::kKey), "
                     "hello::core::kKey) == %d, \"round trip failed for %d\");" % (value, value, value))
    lines += ["", "}  // namespace hello::asserts", ""]
    with open(os.path.normpath(OUT), "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines))
    print("wrote", os.path.normpath(OUT))


if __name__ == "__main__":
    main()
