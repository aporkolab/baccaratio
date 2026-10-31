package com.prokey.baccaratio.model;

import java.util.List;

/** A coherent response value that cannot change during HTTP serialization. */
public record GameSnapshot(int playerChips, BetType betType, int betAmount, String lastResult,
                           List<Card> playerCards, List<Card> bankerCards) {
    public GameSnapshot {
        playerCards = List.copyOf(playerCards);
        bankerCards = List.copyOf(bankerCards);
    }
}
