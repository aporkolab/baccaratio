package com.prokey.baccaratio.controller;

import com.prokey.baccaratio.controller.dto.BetResponse;
import com.prokey.baccaratio.controller.dto.GameResponse;
import com.prokey.baccaratio.model.GameSnapshot;
import com.prokey.baccaratio.service.BaccaratService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/baccarat")
public class BaccaratController {
    private final BaccaratService baccaratService;

    public BaccaratController(BaccaratService baccaratService) {
        this.baccaratService = baccaratService;
    }

    @PostMapping("/bet/{type}/{amount}")
    public BetResponse placeBet(@PathVariable String type, @PathVariable int amount, Principal principal) {
        GameSnapshot game = baccaratService.placeBet(principal.getName(), type, amount);
        String message = String.format("Bet placed on %s with amount %d", game.betType(), game.betAmount());
        return new BetResponse(message, game.playerChips());
    }

    @GetMapping("/play")
    public GameResponse play(Principal principal) {
        GameSnapshot game = baccaratService.playRound(principal.getName());
        return GameResponse.of(game, game.lastResult(), "Round played successfully.");
    }

    @GetMapping("/state")
    public GameResponse getGameState(Principal principal) {
        GameSnapshot game = baccaratService.getGame(principal.getName());
        return GameResponse.of(game, game.lastResult(), "Current game state.");
    }

    @PostMapping("/reset")
    public GameResponse resetGame(Principal principal) {
        GameSnapshot game = baccaratService.resetGame(principal.getName());
        return GameResponse.of(game, "Game reset", "Game has been reset.");
    }
}
