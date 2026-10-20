# Guarded braces 3.0.3

This private, repository-local fork retains the upstream MIT license and API.
Source: https://github.com/micromatch/braces/tree/3.0.3

GHSA-vfj7-8cjw-p6xm has no released upstream fix as of 2026-10-20. The parser now limits nested brace/parenthesis blocks to 127 levels. An iterative check protects compile, expand and stringify from deep or cyclic externally supplied ASTs before recursive traversal. Invalid inputs raise SyntaxError, consistent with the existing maximum-input-length guard. Normal glob lists and ranges are unchanged.

Run `node --test vendor/braces/test.cjs` from the package directory. The dependency override must be removed when an upstream release includes equivalent guards and passes these regressions.
