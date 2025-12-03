# Baccaratio v.1.2.0

[![Build](https://img.shields.io/github/actions/workflow/status/APorkolab/baccaratio/ci.yml?branch=main)](../../actions)
[![License](https://img.shields.io/badge/license-MIT-informational.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/APorkolab/baccaratio.svg)](../../issues)



# Documentation

## Overview

Baccaratio is a comprehensive web application that combines a backend built with Spring Boot 3.2.4 and a front-end developed using Angular 20.2.1. The primary purpose of Baccaratio is to provide a simulated environment for the popular casino game Baccarat, allowing players to place bets, participate in rounds, and monitor their chip balances. The backend is RESTful, ensuring seamless integration with various front-end applications.

## Availability

Baccaratio is also available for demonstration purposes at [https://baccaratio.aporkolab.com/](https://baccaratio.aporkolab.com/), where users can experience the functionality and features of the application firsthand. ***Please be patient, because the backend of the application needs to 'wake up', and the first responses may be slower than normal.***

## Setup

### Prerequisites

- Java JDK 11 or later (the backend was written in Java 17)
- Maven 3.9.6.
- Angular 20.2.1 (upgrade from 17.3.1)

### Setup the backend

1. Clone the repository.
2. Navigate to the project directory.

To build the project, execute the following Maven command:

```shell
mvn clean install
```

3. Run the application using Maven:

```shell 
mvn spring-boot:run
```

The service will start and be available at `http://localhost:8080`.

Ensure JDK 17 and Maven are installed and properly configured in your development environment.


### Setup the frontend

#### Prerequisites

Before setting up the Angular frontend for Baccaratio, ensure you have the following prerequisites installed on your system:

-   Node.js (version 14.x or later)
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
    
    This command will install all the necessary dependencies for the Angular frontend.
    
4.  **Run the Application**:
    
    `npm start` 
    
    This command compiles the application and starts a development server. After successful compilation, the frontend will be available at `http://localhost:4200`.
    
5.  **Access the Application**: Open your web browser and navigate to `http://localhost:4200` to access the Baccaratio frontend.
    

#### Additional Information

-   The Angular frontend provides a modern and responsive user interface for the Baccaratio application.
-   For further customization or development, refer to the Angular documentation and guides.
-   Ensure that the backend server is running locally or is accessible via the specified URL (`http://localhost:8080` by default) to enable full functionality of the Baccaratio application.

By following these installation instructions, you can set up and run the Angular frontend for Baccaratio on your local development environment.

## API Endpoints

#### Base URL

All URLs referenced in the documentation have the base path `/baccarat`.

#### Cross-Origin Resource Sharing (CORS)

The API supports cross-origin requests from `http://localhost:4200`.

### Place a Bet

-   **Endpoint**: `/baccarat/bet/{type}/{amount}`
-   **Method**: `POST`
-   **URL Params**:
    -   `type=[string]` (Options: "player", "banker", "tie")
    -   `amount=[integer]` (The amount of chips to bet)
-   **Success Response**:
    -   **Code**: 200
    -   **Content**: `"Bet placed on: {type} with amount: {amount}"`
-   **Error Response**:
    -   **Code**: 400 BAD REQUEST
    -   **Content**: `"Invalid bet type."` or `"Insufficient chips."`

### Play Round

-   **Endpoint**: `/baccarat/play`
-   **Method**: `GET`
-   **Success Response**:
    -   **Code**: 200
    -   **Content**: `"Round result: {result}. Your bet was: {betType}. Remaining chips: {chips}."`
-   **Notes**:
    -   Initiates a round of Baccarat based on the current bet.

### Get Last Result

-   **Endpoint**: `/baccarat/result`
-   **Method**: `GET`
-   **Success Response**:
    -   **Code**: 200
    -   **Content**: `"Last result: {lastResult}. Your bet was: {betType}. Remaining chips: {chips}."`

## Gameplay Mechanics

In the game of Baccarat, players engage in the following gameplay mechanics:

1.  **Betting Options**:
    
    -   Players can place bets on three outcomes: "player," "banker," or a "tie." Additionally, special betting options may be available for more diverse gameplay.
2.  **Card Dealing**:
    
    -   After players place their bets, the virtual "croupier" initiates the card dealing process.
    -   Cards are dealt to both the player and the banker according to standard Baccarat rules.
3.  **Outcome Determination**:
    
    -   The outcome of the game is determined by comparing the total points of the player's and banker's hands.
    -   The goal is to achieve a total as close to 9 as possible without exceeding it.
4.  **Result Display**:
    
    -   After the cards are dealt, the winning outcome is displayed to the player through an alert within a 5-second window.
5.  **Payouts**:
    
    -   Winning bets are collected at the end of the round and added to the player's chip count.
    -   The amount won depends on the type of bet placed and the corresponding payout ratio.

By following these gameplay mechanics, players can enjoy the excitement and anticipation of Baccarat as they strategize their bets and await the outcome of each round.

## Player Management

Players start with an initial chip count. Bets are deducted at the start of a round, and winnings are added to the player's chip count at the end of the round, according to the outcome and type of bet placed. The maximum bet that one can bet is 100 dollars.

### Baccarat Rules

Baccarat is a popular card game played at casinos worldwide. The game is simple to learn and offers exciting betting opportunities. Here are the basic rules of Baccarat:

1.  **Objective**: The objective of Baccarat is to bet on the hand that will have a total value closest to 9.
    
2.  **Card Values**:
    
    -   Aces count as 1 point.
    -   Cards 2 through 9 retain their face value.
    -   10s and face cards (King, Queen, Jack) count as 0 points.
3.  **Game Setup**:
    
    -   Baccarat is typically played with 6 to 8 decks of cards.
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
        -   If the Banker's total is 4, they draw a third card unless the Player's third card is a 0, 1, 8, or 9.
        -   If the Banker's total is 5, they draw a third card if the Player's third card is a 4, 5, 6, or 7.
        -   If the Banker's total is 6, they draw a third card if the Player's third card is a 6 or 7.
6.  **Determining the Winner**:
    
    -   The hand with a total value closest to 9 wins.
    -   If both hands have the same total, it's a tie.
7.  **Payouts**:
    
    -   Bets on the "Player" hand win even money (1:1).
    -   Bets on the "Banker" hand win even money, but a 5% commission is typically charged on winning bets.
    -   Bets on a "Tie" typically offer higher payouts (usually 8:1 or 9:1), but it's less common due to the higher house edge.

These are the fundamental rules of Baccarat, providing players with an enjoyable and straightforward gaming experience.

## Future Developments

The Baccarat Game API is continuously evolving to meet the growing demands of players and industry standards. The following features are planned for future development:

### Responsive Design for Mobile Devices

Enhancing the responsiveness of the API for mobile devices, ensuring optimal user experience across different screen sizes.

### Continuous Integration and Deployment (CI/CD)

Implementation of CI/CD pipelines to automate the testing, building, and deployment processes, ensuring the reliability and efficiency of updates to the API.

### Enhanced Security Measures

Integration of additional security measures such as JWT authentication and request validation to safeguard player information and prevent unauthorized access.

### Multiplayer Support

Expansion of the API to support multiplayer functionality, enabling multiple players to participate in the game simultaneously and interact with each other.

### Advanced Betting Options

Introduction of advanced betting options such as side bets and progressive jackpots to add depth and excitement to the gameplay experience.

## Legal information

<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />This <span xmlns:dct="http://purl.org/dc/terms/" href="http://purl.org/dc/dcmitype/InteractiveResource" rel="dct:type">work</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="https://aporkolab.com" property="cc:attributionName" rel="cc:attributionURL">Ádám Dr. Porkoláb</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.<br />Based on a work at <a xmlns:dct="http://purl.org/dc/terms/" href="https://github.com/APorkolab/" rel="dct:source">https://github.com/APorkolab/</a>.
