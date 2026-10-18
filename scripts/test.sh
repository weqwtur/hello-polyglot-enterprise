#!/usr/bin/env bash
# Runs the unit tests and a raw shell-pipe smoke test that bypasses the orchestrator.
set -euo pipefail
cd "$(dirname "$0")/.."

echo "== unit tests =="
python3 -m unittest discover -s tests -v

echo "== raw pipe smoke test =="
expected="Hello, World!"
actual=$(java src/java/HelloEnterprise.java "$expected" 2>/dev/null \
  | build/encoder 2>/dev/null \
  | node src/js/relay.js 2>/dev/null \
  | perl src/perl/decoder.pl 2>/dev/null \
  | build/assembler 2>/dev/null \
  | build/printer)
if [[ "$actual" == "$expected" ]]; then
  echo "OK: $actual"
else
  echo "FAIL: expected '$expected', got '$actual'" >&2
  exit 1
fi
