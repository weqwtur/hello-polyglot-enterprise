// GENERATED FILE. Run tools/gen_js.py instead of editing.
'use strict';

/**
 * Middleware 01. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware01(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware01: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware01: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware01: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware01');
  return context;
}

/**
 * Middleware 02. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware02(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware02: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware02: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware02: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware02');
  return context;
}

/**
 * Middleware 03. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware03(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware03: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware03: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware03: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware03');
  return context;
}

/**
 * Middleware 04. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware04(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware04: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware04: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware04: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware04');
  return context;
}

/**
 * Middleware 05. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware05(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware05: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware05: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware05: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware05');
  return context;
}

/**
 * Middleware 06. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware06(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware06: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware06: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware06: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware06');
  return context;
}

/**
 * Middleware 07. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware07(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware07: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware07: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware07: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware07');
  return context;
}

/**
 * Middleware 08. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware08(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware08: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware08: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware08: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware08');
  return context;
}

/**
 * Middleware 09. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware09(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware09: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware09: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware09: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware09');
  return context;
}

/**
 * Middleware 10. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware10(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware10: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware10: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware10: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware10');
  return context;
}

/**
 * Middleware 11. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware11(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware11: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware11: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware11: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware11');
  return context;
}

/**
 * Middleware 12. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware12(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware12: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware12: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware12: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware12');
  return context;
}

/**
 * Middleware 13. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware13(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware13: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware13: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware13: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware13');
  return context;
}

/**
 * Middleware 14. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware14(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware14: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware14: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware14: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware14');
  return context;
}

/**
 * Middleware 15. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware15(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware15: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware15: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware15: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware15');
  return context;
}

/**
 * Middleware 16. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware16(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware16: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware16: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware16: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware16');
  return context;
}

/**
 * Middleware 17. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware17(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware17: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware17: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware17: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware17');
  return context;
}

/**
 * Middleware 18. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware18(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware18: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware18: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware18: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware18');
  return context;
}

/**
 * Middleware 19. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware19(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware19: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware19: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware19: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware19');
  return context;
}

/**
 * Middleware 20. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware20(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware20: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware20: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware20: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware20');
  return context;
}

/**
 * Middleware 21. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware21(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware21: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware21: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware21: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware21');
  return context;
}

/**
 * Middleware 22. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware22(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware22: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware22: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware22: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware22');
  return context;
}

/**
 * Middleware 23. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware23(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware23: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware23: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware23: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware23');
  return context;
}

/**
 * Middleware 24. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware24(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware24: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware24: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware24: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware24');
  return context;
}

/**
 * Middleware 25. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware25(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware25: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware25: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware25: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware25');
  return context;
}

/**
 * Middleware 26. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware26(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware26: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware26: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware26: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware26');
  return context;
}

/**
 * Middleware 27. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware27(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware27: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware27: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware27: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware27');
  return context;
}

/**
 * Middleware 28. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware28(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware28: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware28: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware28: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware28');
  return context;
}

/**
 * Middleware 29. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware29(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware29: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware29: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware29: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware29');
  return context;
}

/**
 * Middleware 30. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware30(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware30: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware30: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware30: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware30');
  return context;
}

/**
 * Middleware 31. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware31(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware31: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware31: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware31: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware31');
  return context;
}

/**
 * Middleware 32. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware32(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware32: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware32: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware32: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware32');
  return context;
}

/**
 * Middleware 33. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware33(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware33: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware33: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware33: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware33');
  return context;
}

/**
 * Middleware 34. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware34(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware34: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware34: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware34: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware34');
  return context;
}

/**
 * Middleware 35. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware35(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware35: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware35: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware35: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware35');
  return context;
}

/**
 * Middleware 36. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware36(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware36: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware36: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware36: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware36');
  return context;
}

/**
 * Middleware 37. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware37(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware37: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware37: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware37: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware37');
  return context;
}

/**
 * Middleware 38. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware38(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware38: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware38: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware38: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware38');
  return context;
}

/**
 * Middleware 39. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware39(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware39: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware39: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware39: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware39');
  return context;
}

/**
 * Middleware 40. Checks the context, leaves a trace, changes nothing.
 * @param {{key: number, values: number[], trace: string[]}} context
 * @returns {{key: number, values: number[], trace: string[]}}
 */
function middleware40(context) {
  if (!context || !Array.isArray(context.values)) {
    throw new TypeError('middleware40: context.values must be an array');
  }
  if (!Number.isInteger(context.key)) {
    throw new TypeError('middleware40: context.key must be an integer');
  }
  for (const value of context.values) {
    if (!Number.isInteger(value) || value < 0 || value > 255) {
      throw new RangeError('middleware40: value out of byte range: ' + value);
    }
  }
  context.trace.push('middleware40');
  return context;
}

const chain = [
  middleware01,
  middleware02,
  middleware03,
  middleware04,
  middleware05,
  middleware06,
  middleware07,
  middleware08,
  middleware09,
  middleware10,
  middleware11,
  middleware12,
  middleware13,
  middleware14,
  middleware15,
  middleware16,
  middleware17,
  middleware18,
  middleware19,
  middleware20,
  middleware21,
  middleware22,
  middleware23,
  middleware24,
  middleware25,
  middleware26,
  middleware27,
  middleware28,
  middleware29,
  middleware30,
  middleware31,
  middleware32,
  middleware33,
  middleware34,
  middleware35,
  middleware36,
  middleware37,
  middleware38,
  middleware39,
  middleware40,
];

module.exports = { chain };
