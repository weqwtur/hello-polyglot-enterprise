#!/usr/bin/env python3
"""Stage 4 fallback (Python): same contract as src/perl/decoder.pl.
Used when Perl is not installed (for example on a plain Windows machine)."""
import sys


def main() -> int:
    lines = [line.strip() for line in sys.stdin.read().splitlines() if line.strip()]
    if len(lines) < 2:
        print("[python-decoder][ERROR] expected KEY line and data line", file=sys.stderr)
        return 1
    parts = lines[0].split()
    if len(parts) != 2 or parts[0] != "KEY" or not parts[1].isdigit():
        print("[python-decoder][ERROR] malformed KEY line: " + lines[0], file=sys.stderr)
        return 1
    key = int(parts[1])
    decoded = [int(token) ^ key for token in lines[1].split()]
    print(" ".join(str(value) for value in decoded))
    print("[python-decoder][INFO ] decoded %d codes with key %d (Perl was unavailable)" % (len(decoded), key),
          file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
