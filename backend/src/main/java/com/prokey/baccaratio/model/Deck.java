package com.prokey.baccaratio.model;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents a deck of cards for the Baccarat game.
 */
public class Deck {
    private static final String[] SUITS = { "Hearts", "Diamonds", "Clubs", "Spades" };
    private static final String[] VALUES = { "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K" };
    private static final int[] POINTS = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0 }; // Points according to Baccarat rules

    private static final Random RANDOM = new SecureRandom();

    private List<Card> cards = new ArrayList<>();

    public Deck() {
        reshuffle();
    }

    /**
     * Reshuffles the deck by clearing existing cards and reinitializing the deck.
     */
    public synchronized void reshuffle() {
        cards.clear();
        for (String suit : SUITS) {
            for (int i = 0; i < VALUES.length; i++) {
                cards.add(new Card(suit, VALUES[i], POINTS[i]));
            }
        }
        Collections.shuffle(cards, RANDOM);
    }

    /**
     * Draws a card from the deck. If the deck is empty, it reshuffles before
     * drawing.
     *
     * @return the drawn card
     */
    public synchronized Card draw() {
        if (cards.isEmpty()) {
            reshuffle();
        }
        return cards.remove(cards.size() - 1);
    }

    public synchronized int size() {
        return cards.size();
    }

    public synchronized List<Card> getCards() {
        return new ArrayList<>(cards); // Returns a copy of the list to prevent external modifications
    }

    public synchronized void setCards(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("Cannot set an empty or null list as the deck.");
        }
        this.cards = new ArrayList<>(cards); // Ensures the deck is a copy of the provided list, not the list itself
    }
}
