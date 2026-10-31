package com.prokey.baccaratio.model;

/**
 * Represents a single playing card used in Baccarat, with a suit, value, and point score.
 *
 * @param suit   the card's suit, e.g. "Hearts", "Diamonds", "Clubs", "Spades"
 * @param value  the card's value, e.g. "A", "2", ..., "K"
 * @param points the card's point value according to Baccarat rules
 */
public record Card(String suit, String value, int points) {

    public Card {
        if (suit == null || suit.isEmpty() || value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Suit and value must not be empty or null.");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative.");
        }
    }
}
