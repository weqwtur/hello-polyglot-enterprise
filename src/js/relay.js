#!/usr/bin/env node
'use strict';
// Stage 3: reads "KEY <k>" and a line of encoded codes, pushes every value through an
// event bus and a 40-step middleware chain, then relays the data unchanged.

const { EventBus } = require('./event_bus');
const { chain } = require('./middleware_chain');

function readStdin() {
  const fs = require('fs');
  return fs.readFileSync(0, 'utf8');
}

function parse(text) {
  const lines = text.split('\n').filter((line) => line.trim().length > 0);
  if (lines.length < 2) {
    throw new Error('expected a KEY line and a data line');
  }
  const match = /^KEY (\d+)$/.exec(lines[0].trim());
  if (!match) {
    throw new Error('malformed KEY line: ' + lines[0]);
  }
  const values = lines[1].trim().split(/\s+/).map((token) => Number.parseInt(token, 10));
  return { key: Number.parseInt(match[1], 10), values, trace: [] };
}

function main() {
  const bus = new EventBus();
  let seen = 0;
  bus.on('value', () => {
    seen += 1;
  });
  bus.on('done', (payload) => {
    process.stderr.write('[js][INFO ] relayed ' + payload.count + ' values through ' + payload.steps + ' middlewares\n');
  });

  let context = parse(readStdin());
  for (const middleware of chain) {
    context = middleware(context);
  }
  for (const value of context.values) {
    bus.emit('value', value);
  }
  bus.emit('done', { count: seen, steps: context.trace.length });

  process.stdout.write('KEY ' + context.key + '\n' + context.values.join(' ') + '\n');
}

try {
  main();
} catch (error) {
  process.stderr.write('[js][ERROR] ' + error.message + '\n');
  process.exit(1);
}
