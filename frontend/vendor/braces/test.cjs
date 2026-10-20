'use strict';
const { test } = require('node:test');
const assert = require('node:assert/strict');
const braces = require('./');
test('preserves lists, ranges, nested groups, escaping and regex compilation', () => {
  assert.deepEqual(braces.expand('src/{app,lib}/{a,b}.ts'), ['src/app/a.ts','src/app/b.ts','src/lib/a.ts','src/lib/b.ts']);
  assert.deepEqual(braces.expand('{1..3}'), ['1','2','3']);
  assert.deepEqual(braces.expand('{a,{b,c}}'), ['a','b','c']);
  assert.equal(braces.compile('a/{b,c}/d'), 'a/(b|c)/d');
  assert.equal(braces.stringify(braces.parse('a/{b,c}/d')), 'a/{b,c}/d');
  assert.deepEqual(braces.expand('literal'), ['literal']);
});
test('rejects deeply nested strings before recursive walkers run', () => {
  for (const [open,close] of [['{','}'],['(',')']]) {
    const input = open.repeat(3000) + 'a,b' + close.repeat(3000);
    for (const fn of [braces,braces.parse,braces.compile,braces.expand,braces.stringify]) {
      assert.throws(() => fn(input), { name: 'SyntaxError', message: /maximum nesting depth/ });
    }
  }
});
test('guards externally supplied deep and cyclic ASTs', () => {
  let ast = { type: 'text', value: 'a' };
  for (let i=0;i<3000;i++) ast = { type: 'brace', nodes: [ast] };
  for (const fn of [braces.compile,braces.expand,braces.stringify]) assert.throws(() => fn(ast), SyntaxError);
  const cycle = { type: 'root', nodes: [] }; cycle.nodes.push(cycle);
  for (const fn of [braces.compile,braces.expand,braces.stringify]) assert.throws(() => fn(cycle), SyntaxError);
});
