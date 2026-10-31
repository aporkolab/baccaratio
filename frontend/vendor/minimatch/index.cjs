'use strict';
// Preserve the callable CommonJS export used by glob 7 and Karma.
const api = require('./dist/commonjs/index.js');
module.exports = Object.assign(api.minimatch, api);
