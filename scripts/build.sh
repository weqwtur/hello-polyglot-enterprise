#!/usr/bin/env bash
# Compiles every stage that has an available toolchain.
set -euo pipefail
cd "$(dirname "$0")/.."
python3 orchestrator/main.py --build-only
