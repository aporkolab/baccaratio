const assert = require('node:assert/strict');
const { test } = require('node:test');
const { spawnSync } = require('node:child_process');
const pm = require('picomatch');

test('repeated wildcard extglobs cannot block the event loop', () => {
  const run = spawnSync(process.execPath, ['-e', `
    const assert = require('node:assert/strict');
    const pm = require('picomatch');
    for (const options of [{}, { maxExtglobRecursion: false }]) {
      for (const pattern of ['+(*)', '*(*)', '+(*a)', '*(*a)', '+(*?a)', '+(?*a)', '+(@(*a))', '+(+(a))']) {
        for (const suffix of ['!', '/']) {
          assert.equal(pm(pattern, options)('a'.repeat(40) + suffix), false);
        }
      }
    }
  `], { timeout: 5000, encoding: 'utf8' });
  assert.ifError(run.error);
  assert.equal(run.status, 0, run.stderr);
});

test('ordinary globs, safe extglobs, escaping and scanner APIs still work', () => {
  assert.ok(pm('src/**/*.ts')('src/app/test.ts'));
  assert.ok(pm('file.@(js|ts)')('file.ts'));
  assert.ok(pm('file.+(a|b).{js,ts}')('file.abab.ts'));
  assert.ok(pm('+([*]a)')('*a*a'));
  assert.ok(pm(String.raw`+(\*a)`)('*a*a'));
  assert.ok(pm('+(*(a)|*(b))')('abba'));
  assert.ok(pm.makeRe('src/**/*.js').test('src/app/test.js'));
  assert.equal(pm.scan('src/**/*.js').isGlob, true);
});
