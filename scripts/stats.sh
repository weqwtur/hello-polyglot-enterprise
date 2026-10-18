#!/usr/bin/env bash
# Prints line counts per language, for bragging purposes.
set -euo pipefail
cd "$(dirname "$0")/.."
count() { find "$@" -type f 2>/dev/null | xargs cat 2>/dev/null | wc -l; }
printf "%-12s %6s\n" "Java"       "$(count src/java -name '*.java')"
printf "%-12s %6s\n" "C"          "$(count src/c \( -name '*.c' -o -name '*.h' \))"
printf "%-12s %6s\n" "C++"        "$(count src/cpp \( -name '*.cpp' -o -name '*.hpp' \))"
printf "%-12s %6s\n" "JavaScript" "$(count src/js -name '*.js')"
printf "%-12s %6s\n" "Perl"       "$(count src/perl -name '*.pl')"
printf "%-12s %6s\n" "Assembly"   "$(count src/asm -name '*.s')"
printf "%-12s %6s\n" "Go"         "$(count src/go -name '*.go')"
printf "%-12s %6s\n" "Rust"       "$(count src/rust -name '*.rs')"
printf "%-12s %6s\n" "Python"     "$(count orchestrator tools tests src/python -name '*.py')"
printf "%-12s %6s\n" "Bash"       "$(count scripts -name '*.sh')"
printf "%-12s %6s\n" "Configs"    "$(count config -type f)"
echo "------------------"
total=$(find src orchestrator tools tests scripts config Makefile Dockerfile docker-compose.yml -type f 2>/dev/null | xargs cat | wc -l)
printf "%-12s %6s\n" "TOTAL" "$total"
