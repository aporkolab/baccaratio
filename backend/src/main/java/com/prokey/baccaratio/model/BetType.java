package com.prokey.baccaratio.model;

import java.util.Locale;

/**
 * The bets a player can place on a Baccarat round.
 */
public enum BetType {
    PLAYER,
    BANKER,
    TIE,
    PLAYERPAIR,
    BANKERPAIR,
    EITHERPAIR,
    PERFECTPAIRONE;

    /**
     * Parses a bet type case-insensitively (e.g. "playerPair" or "PLAYERPAIR").
     *
     * @throws IllegalArgumentException if the value is not a known bet type
     */
    public static BetType parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invalid bet type.");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid bet type.", e);
        }
    }
}
