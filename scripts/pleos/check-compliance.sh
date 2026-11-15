#!/usr/bin/env sh
# Pleos guide compliance checks for ourroad (macOS, Linux, Git Bash).
# The checks live in check_compliance.py so Windows can run the same logic with
#   py -3 scripts\pleos\check_compliance.py
# Options: --static-only (skip Gradle), --strict (MINOR also fails), --list-manual
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

if command -v python3 >/dev/null 2>&1; then
    PYTHON=python3
elif command -v python >/dev/null 2>&1; then
    PYTHON=python
else
    echo "BLOCKED: Python 3.9+ is required (python3 not found)" >&2
    exit 2
fi

exec "$PYTHON" -I "$SCRIPT_DIR/check_compliance.py" "$@"
