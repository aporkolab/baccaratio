'use strict';

const MAX_DEPTH = 128;
const assertDepth = ast => {
  const pending = [[ast, 0]];
  const seen = new Set();
  while (pending.length) {
    const [node, depth] = pending.pop();
    if (!node || typeof node !== 'object') continue;
    if (depth >= MAX_DEPTH || seen.has(node)) {
      throw new SyntaxError('Brace pattern exceeds maximum nesting depth');
    }
    seen.add(node);
    if (Array.isArray(node.nodes)) {
      for (const child of node.nodes) pending.push([child, depth + 1]);
    }
  }
};
module.exports = { MAX_DEPTH, assertDepth };
