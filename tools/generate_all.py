#!/usr/bin/env python3
"""Regenerates every generated source file in the project."""
import importlib.util
import os

HERE = os.path.dirname(os.path.abspath(__file__))
GENERATORS = ["gen_java.py", "gen_cpp.py", "gen_js.py", "gen_c.py"]


def load(name):
    spec = importlib.util.spec_from_file_location(name[:-3], os.path.join(HERE, name))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def main():
    for name in GENERATORS:
        load(name).main()


if __name__ == "__main__":
    main()
