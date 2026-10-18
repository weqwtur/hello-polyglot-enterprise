#!/usr/bin/env python3
"""Generates src/js/middleware_chain.js: 40 middleware functions that each
verify the context and change nothing."""
import os

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "js", "middleware_chain.js")

TEMPLATE = """/**
 * Middleware %NN%. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware%NN%(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware%NN%: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware%NN%: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware%NN%: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware%NN%');
  return context;
}

"""


def main():
    parts = ["// GENERATED FILE. Run tools/gen_js.py instead of editing.\n'use strict';\n\n"]
    for n in range(1, 41):
        parts.append(TEMPLATE.replace("%NN%", "%02d" % n))
    parts.append("const chain = [\n")
    for n in range(1, 41):
        parts.append("  middleware%02d,\n" % n)
    parts.append("];\n\nmodule.exports = { chain };\n")
    with open(os.path.normpath(OUT), "w", encoding="utf-8") as handle:
        handle.write("".join(parts))
    print("wrote", os.path.normpath(OUT))


if __name__ == "__main__":
    main()
