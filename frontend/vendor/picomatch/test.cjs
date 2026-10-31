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

test('unescaping malformed bracket runs completes in linear time', () => {
  const run = spawnSync(process.execPath, ['-e', `
    const assert = require('node:assert/strict');
    const pm = require('picomatch');
    const input = '['.repeat(100000) + String.fromCharCode(92) + 'q';
    const result = pm.scan(input, { unescape: true });
    assert.equal(result.base + result.glob, '['.repeat(100000) + 'q');
  `], { timeout: 5000, encoding: 'utf8' });
  assert.ifError(run.error);
  assert.equal(run.status, 0, run.stderr);
});

test('unescaping preserves bracket classes, trailing escapes and line terminators', () => {
  const { removeBackslashes } = require('./lib/utils');
  const cases = [
    [String.raw`foo\bar`, 'foobar'],
    [String.raw`[a\b]`, String.raw`[a\b]`],
    [String.raw`[a\]b]`, String.raw`[a\]b]`],
    [String.raw`[a\]`, '[a]'],
    [String.raw`[][\q`, '[][q'],
    [String.raw`[[]\q`, '[[]q'],
    ['trailing' + '\\', 'trailing' + '\\'],
    ['\\\\q', 'q'],
    ['[\n]\\q', '[\n]q'],
    ['[\n\\q]', '[\nq]'],
    ...['\n', '\r', '\u2028', '\u2029'].map(line => ['\\' + line, '\\' + line])
  ];
  for (const [input, expected] of cases) assert.equal(removeBackslashes(input), expected);
});
