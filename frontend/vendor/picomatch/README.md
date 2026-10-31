# picomatch repeated-wildcard security guard

Source: npm picomatch 4.0.7, under the included MIT license. This is a separately
named local fork, not a published upstream fix. CVE-2026-112366 /
SNYK-JS-PICOMATCH-21248802 has no fixed upstream version as of 2026-10-31.

The existing repeated-extglob guard skipped one-branch bodies such as `+(*)`
and `+(*a)`. The patch checks for unbounded wildcard repetition before that
branch-count gate (also when maxExtglobRecursion is disabled). It follows the
existing parser's fail-closed behavior: risky repeated extglobs are literalized.
Escaped stars, quoted stars and bracketed stars remain literals; ordinary glob,
brace, extension and simple extglob matching are retained. Nested plus repeats
are rejected too. This intentionally changes the matching behavior of dangerous
patterns. No application glob patterns depend on those forms.

The unescaping helper also uses a linear two-pass scan instead of the upstream
backtracking bracket regex (flagged by CodeQL). It preserves bracket contents,
escaped closing brackets and line-terminator behavior without repeatedly
rescanning malformed runs of opening brackets. Tests cover both the timeout
regression and the existing unescape behavior.

The patch does not enable arbitrary untrusted regular expressions. Replace it
with an upstream fixed version when one is released, retaining the isolated
process timeout regressions in test.cjs.
