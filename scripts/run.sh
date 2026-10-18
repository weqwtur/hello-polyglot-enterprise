#!/usr/bin/env bash
# Runs the whole pipeline. Extra arguments are passed to the orchestrator,
# e.g. ./scripts/run.sh --message "Hello, Pasha!" --verbose
set -euo pipefail
cd "$(dirname "$0")/.."
python3 orchestrator/main.py "$@"
