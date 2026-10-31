package com.prokey.baccaratio.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeckTest {

    @Test
    void newDeckHas52DistinctCardsWithBaccaratPoints() {
        List<Card> cards = new Deck().getCards();

        assertEquals(52, cards.size());
        assertEquals(52, new HashSet<>(cards).size());
        for (Card card : cards) {
            int expected = switch (card.value()) {
                case "A" -> 1;
                case "10", "J", "Q", "K" -> 0;
                default -> Integer.parseInt(card.value());
            };
            assertEquals(expected, card.points(), card.toString());
        }
    }

    @Test
    void drawRemovesCardsAndReshufflesWhenEmpty() {
        Deck deck = new Deck();
        Set<Card> drawn = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            drawn.add(deck.draw());
        }
        assertEquals(52, drawn.size());
        assertEquals(0, deck.size());

        deck.draw();
        assertEquals(51, deck.size());
    }

    @Test
    void setCardsUsesADefensiveCopyAndRejectsEmptyInput() {
        Deck deck = new Deck();
        Card ace = new Card("Spades", "A", 1);
        List<Card> source = new java.util.ArrayList<>(List.of(ace));
        deck.setCards(source);
        source.clear();

        assertEquals(ace, deck.draw());
        assertThrows(IllegalArgumentException.class, () -> deck.setCards(List.of()));
        assertThrows(IllegalArgumentException.class, () -> deck.setCards(null));
    }

    @Test
    void cardRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new Card("", "A", 1));
        assertThrows(IllegalArgumentException.class, () -> new Card("Hearts", null, 1));
        assertThrows(IllegalArgumentException.class, () -> new Card("Hearts", "A", -1));
    }

    @Test
    void betTypeParsingIsCaseInsensitive() {
        assertEquals(BetType.PLAYERPAIR, BetType.parse("playerPair"));
        assertEquals(BetType.BANKER, BetType.parse(" banker "));
        assertThrows(IllegalArgumentException.class, () -> BetType.parse("dragon"));
        assertThrows(IllegalArgumentException.class, () -> BetType.parse(null));
    }
}
