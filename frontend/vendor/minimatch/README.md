# minimatch 10 compatibility security backport

Source: npm minimatch 10.2.6, under the included Blue Oak license.
This package has its own name and version and is not an upstream release.

GHSA-g3hv-75wg-m249 is fixed by backporting the minimatch 11.0.1 changes
(upstream commit 77a0f5c): limit first-phase globparts expansion to braceExpandMax
(default 100000) and skip quadratic second-phase optimization above 1000 parts.
Both CommonJS and ESM builds contain the same patch. Matching remains correct
when the optional optimization is skipped. The legacy makeRe API is retained;
minimatch 11 removed it, breaking current ESLint and older glob/Karma consumers.
The CJS entry also exposes the callable form used by pre-v5 callers.

Replace this override once all callers support upstream minimatch 11 or a fixed
compatible release. Keep the security and compatibility regression tests.

Unused parser assignments reported by CodeQL have also been removed from both
builds; this does not change matching behavior.
