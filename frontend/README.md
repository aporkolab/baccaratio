# Baccaratio

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 16.2.6 and is maintained on Angular CLI 22.2. It requires Node.js 24.

## Development server

Run `npx ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files. The development configuration talks to the backend at `http://localhost:8080`.

## Code scaffolding

Run `npx ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum`.

## Build

Run `npm run build` to build the project for production. The build artifacts will be stored in the `dist/baccaratio` directory and can be served with `npm start`.

The backend URL is injected at build time from `API_URL` (a `.env` file in this directory or an environment variable); it defaults to `https://api.baccaratio.porkolab.hu`.

## Running unit tests

Run `npm test` to execute the unit tests via [Karma](https://karma-runner.github.io). For a single headless run, as in CI, use `npm test -- --watch=false --browsers=ChromeHeadless`.

`npm run test:dependency-guards` runs the regression tests of the vendored `braces` fork.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
