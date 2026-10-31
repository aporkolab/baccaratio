const assert = require('node:assert/strict');
const { test } = require('node:test');
const { spawnSync } = require('node:child_process');

test('globpart amplification stays bounded while legacy matching APIs work', () => {
  const run = spawnSync(process.execPath, ['-e', `
    const m = require('minimatch');
    const assert = require('node:assert/strict');
    const matcher = new m.Minimatch('a/**/../b/'.repeat(18) + 'x', { optimizationLevel: 2, braceExpandMax: 64 });
    assert.ok(matcher.globParts.length <= 64);
    assert.ok(m('src/app/test.ts', 'src/**/*.ts'));
    assert.ok(m.makeRe('src/**/*.ts').test('src/app/test.ts'));
  `], { timeout: 5000, encoding: 'utf8' });
  assert.ifError(run.error);
  assert.equal(run.status, 0, run.stderr);
});

test('ESM consumers receive the patched parser and makeRe export', async () => {
  const { Minimatch, makeRe } = await import('minimatch');
  const matcher = new Minimatch('a/**/../b/'.repeat(18) + 'x', { optimizationLevel: 2, braceExpandMax: 64 });
  assert.ok(matcher.globParts.length <= 64);
  assert.ok(makeRe('src/**/*.ts').test('src/app/test.ts'));
});
