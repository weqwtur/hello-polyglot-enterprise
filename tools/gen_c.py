#!/usr/bin/env python3
"""Generates src/c/ascii_guard.c and ascii_guard.h: one dedicated validator
function per printable ASCII character, plus a dispatch switch and a name table."""
import os

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "c")
LABELS = {32: "SPACE", 34: "QUOTE", 39: "APOSTROPHE", 42: "ASTERISK", 47: "SLASH", 92: "BACKSLASH",
          63: "QUESTION", 37: "PERCENT"}


def label(code):
    return LABELS.get(code, chr(code))


def main():
    header = """/* GENERATED FILE. Run tools/gen_c.py instead of editing. */
#ifndef ASCII_GUARD_H
#define ASCII_GUARD_H

/* Returns 1 if value is a printable ASCII code accepted by its dedicated validator. */
int ascii_guard_is_valid(int value);

/* Returns a human readable name for the code, or "UNKNOWN". */
const char *ascii_guard_name(int value);

/* Number of dedicated validators. */
int ascii_guard_count(void);

#endif /* ASCII_GUARD_H */
"""
    lines = ["/* GENERATED FILE. Run tools/gen_c.py instead of editing. */",
             '#include "ascii_guard.h"', ""]
    for code in range(32, 127):
        lines += [
            "/* Validator for code %d (%s). */" % (code, label(code)),
            "static int check_%03d(int value) {" % code,
            "    if (value != %d) {" % code,
            "        return 0;",
            "    }",
            "    return 1;",
            "}",
            "",
        ]
    lines += ["int ascii_guard_is_valid(int value) {", "    switch (value) {"]
    for code in range(32, 127):
        lines.append("    case %d:" % code)
        lines.append("        return check_%03d(value);" % code)
    lines += ["    default:", "        return 0;", "    }", "}", ""]

    lines.append("static const char *const NAMES[95] = {")
    for code in range(32, 127):
        lines.append('    "%s",' % label(code))
    lines += ["};", ""]
    lines += ["const char *ascii_guard_name(int value) {",
              "    if (value < 32 || value > 126) {",
              '        return "UNKNOWN";',
              "    }",
              "    return NAMES[value - 32];",
              "}", "",
              "int ascii_guard_count(void) {",
              "    return 95;",
              "}", ""]
    base = os.path.normpath(BASE)
    with open(os.path.join(base, "ascii_guard.h"), "w", encoding="utf-8") as handle:
        handle.write(header)
    with open(os.path.join(base, "ascii_guard.c"), "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines))
    print("wrote", base)


if __name__ == "__main__":
    main()
