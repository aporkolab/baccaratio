/* eslint-disable @typescript-eslint/no-var-requires */
const webpack = require("webpack");
const fs = require("fs");
const path = require("path");
const dotenv = require("dotenv");

const envPath = path.resolve(__dirname, ".env");

// Values from .env (if present) take precedence; otherwise fall back to the process environment.
// Only API_URL is read: never inject the whole environment into the browser bundle.
const fileEnv = fs.existsSync(envPath) ? dotenv.parse(fs.readFileSync(envPath)) : {};
const apiUrl = (fileEnv.API_URL || process.env.API_URL || "").trim().replace(/\/+$/, "");

// ngx-build-plus merges this object into Angular's webpack config with webpack-merge
// (a function export is not invoked by --extra-webpack-config).
module.exports = {
  plugins: [
    new webpack.DefinePlugin({
      // Read by src/environments/environment.ts; an empty value selects the default API URL there
      API_BASE_URL: JSON.stringify(apiUrl),
    }),
  ],
};
