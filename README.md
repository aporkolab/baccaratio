# Baccaratio v.1.2.0

[![Build](https://img.shields.io/github/actions/workflow/status/APorkolab/baccaratio/ci.yml?branch=main)](../../actions)
[![License](https://img.shields.io/badge/license-MIT-informational.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/APorkolab/baccaratio.svg)](../../issues)



# Documentation

## Overview

Baccaratio is a comprehensive web application that combines a backend built with Spring Boot 4.1 and a front-end developed using Angular 22. The primary purpose of Baccaratio is to provide a simulated environment for the popular casino game Baccarat, allowing players to place bets, participate in rounds, and monitor their chip balances. The backend is RESTful and stateless (JWT authentication), ensuring seamless integration with various front-end applications.

### Tech stack

| Part | Versions |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.2 (Spring Framework 7, Spring Security 7, Hibernate 7), H2 (in-memory), jjwt 0.13, springdoc-openapi 3.2 |
| Frontend | Angular 22.2, TypeScript 6.0, RxJS 7.8, Karma + Jasmine |
| Tooling | Maven 3.9 (wrapper included), Node.js 24, npm 11 |

## Setup

### Prerequisites

- Java JDK 21 or later
- Maven 3.9+ (or the bundled Maven wrapper: `./mvnw`)
- Node.js 24 (version 24.15.0 or later within the 24.x line) and npm

### Setup the backend

1. Clone the repository.
2. Navigate to the `backend` directory.

To build the project and run the unit and integration tests, execute the following Maven command:

```shell
mvn clean verify
```

3. Run the application using Maven:

```shell 
mvn spring-boot:run
```

The service will start and be available at `http://localhost:8080`. The interactive API documentation is served at `http://localhost:8080/swagger-ui.html`.

To build a deployable archive without running the tests, use `mvn -DskipTests package`; the executable `target/baccaratio-1.0.0.war` can be started with `java -jar`.

#### Configuration (environment variables)

| Variable | Default | Description |
| --- | --- | --- |
| `JWT_SECRET` | *(random key per start)* | Base64 encoded HMAC key of at least 256 bits used to sign the JWTs, e.g. generated with `openssl rand -base64 32`. Set it in production; without it every restart invalidates the issued tokens. |
| `JWT_EXPIRATION` | `PT10H` | Token lifetime as an ISO-8601 duration. |
| `CORS_ORIGIN` | *(empty)* | Comma separated list of additional allowed origins. The production domains and `http://localhost:4200` are always allowed. |
| `H2_CONSOLE_ENABLED` | `false` | Enables the H2 web console at `/h2-console`. Only enable it for local development: the console executes arbitrary SQL. |
| `JPA_SHOW_SQL` | `false` | Logs the executed SQL statements. |
| `PORT` | `8080` | HTTP port (used by the `Procfile`, pass it as `-Dserver.port`). |

The data is stored in an in-memory H2 database, so users and balances are reset whenever the backend restarts.

### Setup the frontend

#### Prerequisites

Before setting up the Angular frontend for Baccaratio, ensure you have the following prerequisites installed on your system:

-   Node.js 24 (version 24.15.0 or later within the 24.x line)
-   npm (Node Package Manager, typically installed with Node.js)

#### Setup

Follow these steps to set up and run the Angular frontend for Baccaratio:

1.  **Clone the Repository**:
    
    `git clone <repository-url>` 
    
    Replace `<repository-url>` with the URL of the Baccaratio repository.
    
2.  **Navigate to the Frontend Directory**:
    
    `cd <repository-directory>/frontend` 
    
    Replace `<repository-directory>` with the directory where you cloned the repository.
    
3.  **Install Dependencies**:
    
    `npm install` 
    
    This command will install all the necessary dependencies for the Angular frontend (the `postinstall` script also produces a production build).
    
4.  **Run the Application**:

    - Development server with live reload, talking to the backend at `http://localhost:8080`:

      `npx ng serve`

      The frontend will be available at `http://localhost:4200`.

    - Production build served statically (this is what `npm start` does, e.g. on Heroku):

      `npm run build && npm start`

5.  **Access the Application**: Open your web browser and navigate to `http://localhost:4200` (development server) or the address printed by `serve`.

#### Configuration

The production build reads the backend URL from the `API_URL` variable, either from a `frontend/.env` file (`API_URL=https://api.example.com`) or from the environment of the build. Without it the build falls back to `https://api.baccaratio.porkolab.hu`. Only `API_URL` is injected into the bundle.

#### Tests

```shell
npm test -- --watch=false --browsers=ChromeHeadless   # unit tests (Karma + Jasmine)
npm run test:dependency-guards                         # regression tests of the vendored braces fork
npm audit --audit-level=high                           # dependency audit, as run in CI
```

#### Additional Information

-   The Angular frontend provides a modern and responsive user interface for the Baccaratio application.
-   For further customization or development, refer to the Angular documentation and guides.
-   Ensure that the backend server is running locally or is accessible via the specified URL (`http://localhost:8080` by default) to enable full functionality of the Baccaratio application.

By following these installation instructions, you can set up and run the Angular frontend for Baccaratio on your local development environment.

### Continuous Integration

The `CI + Tools` GitHub Actions workflow (`.github/workflows/ci.yml`) runs on pushes to `main` and on pull requests:

- **Backend (Java/Maven)**: `mvn -B verify` (unit tests with Surefire, `*IT` integration tests with Failsafe).
- **Frontend (CI)**: `npm ci`, the dependency guard tests, `npm audit --audit-level=high`, the production build and the Karma tests in headless Chrome.

Dependabot keeps the npm, Maven and GitHub Actions dependencies up to date.

## API Endpoints

#### Base URL

The authentication endpoints live under `/auth`, the game endpoints under `/baccarat`. The OpenAPI description is available at `/v3/api-docs` and `/swagger-ui.html`.

#### Authentication

All `/baccarat` endpoints require a JWT in the `Authorization: Bearer <token>` header. Requests without a valid (or with an expired) token are answered with `401 Unauthorized`.

-   **Register**: `POST /auth/register` with `{"username": "...", "password": "..."}`
    -   Username: 3-50 characters (letters, digits, `.`, `_`, `-`); password: 6-72 characters.
    -   **Success**: `200` with `{"id": 1, "username": "..."}`
    -   **Error**: `400` for invalid input or an already taken username.
-   **Login**: `POST /auth/login` with `{"username": "...", "password": "..."}`
    -   **Success**: `200` with `{"token": "<jwt>"}`
    -   **Error**: `401` with the message `Invalid username or password.`

#### Cross-Origin Resource Sharing (CORS)

The API supports cross-origin requests from the production frontend domains and `http://localhost:4200`; further origins can be added with `CORS_ORIGIN`.

#### Game state

Every user has one table on the server; it is identified by the authenticated user, not by a cookie or session. New players start with 1000 chips. The game responses share this format:

```json
{
  "result": "Player",
  "playerCards": [{ "suit": "Hearts", "value": "9", "points": 9 }],
  "bankerCards": [{ "suit": "Clubs", "value": "K", "points": 0 }],
  "playerChips": 1100,
  "message": "Round played successfully."
}
```

### Place a Bet

-   **Endpoint**: `/baccarat/bet/{type}/{amount}`
-   **Method**: `POST`
-   **URL Params**:
    -   `type=[string]` (case-insensitive; options: `player`, `banker`, `tie`, `playerPair`, `bankerPair`, `eitherPair`, `perfectPairOne`)
    -   `amount=[integer]` (The amount of chips to bet, positive and not more than the balance)
-   **Success Response**:
    -   **Code**: 200
    -   **Content**: `{"message": "Bet placed on PLAYER with amount 100", "chips": 1000}`
-   **Error Response**:
    -   **Code**: 400 BAD REQUEST
    -   **Content**: error object with the message `"Invalid bet type."`, `"Bet amount must be positive."` or `"Insufficient chips for this bet."`

### Play Round

-   **Endpoint**: `/baccarat/play`
-   **Method**: `GET`
-   **Success Response**:
    -   **Code**: 200
    -   **Content**: game response; `result` is `Player`, `Banker` or `Tie` (with ` You have run out of chips.` appended when the balance reaches zero).
-   **Error Response**:
    -   **Code**: 409 CONFLICT when no bet has been placed or the balance no longer covers the bet.
-   **Notes**:
    -   Initiates a round of Baccarat based on the current bet. The bet stays in place for the following rounds until a new bet is placed.

### Get Game State

-   **Endpoint**: `/baccarat/state`
-   **Method**: `GET`
-   **Success Response**:
    -   **Code**: 200
    -   **Content**: game response with the last result, the cards of the last round and the current balance.

### Reset Game

-   **Endpoint**: `/baccarat/reset`
-   **Method**: `POST`
-   **Success Response**:
    -   **Code**: 200
    -   **Content**: game response with `"result": "Game reset"` and the balance restored to 1000 chips.

### Error format

Errors are returned as JSON:

```json
{
  "timestamp": "2026-10-31T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient chips for this bet.",
  "path": "/baccarat/bet/player/5000"
}
```

## Gameplay Mechanics

In the game of Baccarat, players engage in the following gameplay mechanics:

1.  **Betting Options**:
    
    -   Players can place bets on three outcomes: "player," "banker," or a "tie." Additionally, the pair side bets "player pair" (11:1), "banker pair" (11:1), "either pair" (5:1) and "perfect pair" (25:1) are available.
2.  **Card Dealing**:
    
    -   After players place their bets, the virtual "croupier" initiates the card dealing process.
    -   Cards are dealt to both the player and the banker according to standard Baccarat rules.
3.  **Outcome Determination**:
    
    -   The outcome of the game is determined by comparing the total points of the player's and banker's hands.
    -   The goal is to achieve a total as close to 9 as possible without exceeding it.
4.  **Result Display**:
    
    -   The cards are dealt one by one on the table, and the winning outcome is displayed to the player as a notification once the round is over.
5.  **Payouts**:
    
    -   Winning bets are collected at the end of the round and added to the player's chip count.
    -   The amount won depends on the type of bet placed and the corresponding payout ratio.

By following these gameplay mechanics, players can enjoy the excitement and anticipation of Baccarat as they strategize their bets and await the outcome of each round.

## Player Management

Players start with 1000 chips. Bets are deducted at the start of a round, and winnings are added to the player's chip count at the end of the round, according to the outcome and type of bet placed. A bet can be at most the player's current balance; a push (tie on a player or banker bet) returns the stake. The balance can be reset to 1000 chips at any time.

### Baccarat Rules

Baccarat is a popular card game played at casinos worldwide. The game is simple to learn and offers exciting betting opportunities. Here are the basic rules of Baccarat:

1.  **Objective**: The objective of Baccarat is to bet on the hand that will have a total value closest to 9.
    
2.  **Card Values**:
    
    -   Aces count as 1 point.
    -   Cards 2 through 9 retain their face value.
    -   10s and face cards (King, Queen, Jack) count as 0 points.
3.  **Game Setup**:
    
    -   Baccarat is typically played with 6 to 8 decks of cards; Baccaratio deals from a single 52-card deck that is reshuffled when fewer than 15 cards remain.
    -   Each round begins with players placing their bets on either the "Player," "Banker," or "Tie" outcome.
4.  **Dealing Cards**:
    
    -   The dealer deals two cards each to the "Player" and "Banker" hands.
    -   If either hand has a total value of 8 or 9 from the first two cards (known as a "natural"), no further cards are drawn.
5.  **Drawing Additional Cards**:
    
    -   If the total value of either hand is less than 8 or 9, additional cards may be drawn according to specific rules:
        -   If the Player's total is 5 or less, the Player draws another card. Otherwise, the Player stands.
        -   If the Player stands, the Banker draws a card if their total is 5 or less.
        -   If the Banker's total is 2 or less, they always draw a card.
        -   If the Banker's total is 3, they draw a third card unless the Player's third card is an 8.
        -   If the Banker's total is 4, they draw a third card if the Player's third card is a 2, 3, 4, 5, 6, or 7.
        -   If the Banker's total is 5, they draw a third card if the Player's third card is a 4, 5, 6, or 7.
        -   If the Banker's total is 6, they draw a third card if the Player's third card is a 6 or 7.
6.  **Determining the Winner**:
    
    -   The hand with a total value closest to 9 wins.
    -   If both hands have the same total, it's a tie.
7.  **Payouts**:
    
    -   Bets on the "Player" hand win even money (1:1).
    -   Bets on the "Banker" hand win even money minus a 5% commission (0.95:1, rounded down to whole chips).
    -   Bets on a "Tie" pay 8:1.

These are the fundamental rules of Baccarat, providing players with an enjoyable and straightforward gaming experience.

## Future Developments

The Baccarat Game API is continuously evolving to meet the growing demands of players and industry standards. The following features are planned for future development:

### Responsive Design for Mobile Devices

Enhancing the responsiveness of the API for mobile devices, ensuring optimal user experience across different screen sizes.

### Continuous Deployment

The CI pipeline already builds and tests every change; automated deployment of the backend and frontend is planned.

### Persistent Storage

Replacing the in-memory H2 database with a persistent database so that accounts and balances survive restarts.

### Multiplayer Support

Expansion of the API to support multiplayer functionality, enabling multiple players to participate in the game simultaneously and interact with each other.

### Advanced Betting Options

Introduction of advanced betting options such as side bets and progressive jackpots to add depth and excitement to the gameplay experience.

## Legal information

<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />This <span xmlns:dct="http://purl.org/dc/terms/" href="http://purl.org/dc/dcmitype/InteractiveResource" rel="dct:type">work</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="https://aporkolab.com" property="cc:attributionName" rel="cc:attributionURL">Ádám Dr. Porkoláb</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.<br />Based on a work at <a xmlns:dct="http://purl.org/dc/terms/" href="https://github.com/APorkolab/" rel="dct:source">https://github.com/APorkolab/</a>.
