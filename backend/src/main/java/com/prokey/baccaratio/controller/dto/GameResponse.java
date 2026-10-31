package com.prokey.baccaratio.controller.dto;

import com.prokey.baccaratio.model.Card;
import com.prokey.baccaratio.model.GameSnapshot;

import java.util.List;

public record GameResponse(String result, List<Card> playerCards, List<Card> bankerCards, int playerChips,
                           String message) {

    public static GameResponse of(GameSnapshot game, String result, String message) {
        return new GameResponse(result, game.playerCards(), game.bankerCards(), game.playerChips(), message);
    }
}
