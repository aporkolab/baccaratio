package com.prokey.baccaratio.controller.dto;

import com.prokey.baccaratio.model.Card;
import com.prokey.baccaratio.model.Game;

import java.util.List;

public record GameResponse(String result, List<Card> playerCards, List<Card> bankerCards, int playerChips,
                           String message) {

    public static GameResponse of(Game game, String result, String message) {
        return new GameResponse(result, game.getPlayerCards(), game.getBankerCards(),
                game.getPlayer().getChips(), message);
    }
}
