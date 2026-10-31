package com.prokey.baccaratio.controller;

import com.prokey.baccaratio.controller.dto.BetResponse;
import com.prokey.baccaratio.controller.dto.GameResponse;
import com.prokey.baccaratio.model.Game;
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
        Game game = baccaratService.placeBet(principal.getName(), type, amount);
        String message = String.format("Bet placed on %s with amount %d", game.getBetType(), amount);
        return new BetResponse(message, game.getPlayer().getChips());
    }

    @GetMapping("/play")
    public GameResponse play(Principal principal) {
        Game game = baccaratService.playRound(principal.getName());
        return GameResponse.of(game, game.getLastResult(), "Round played successfully.");
    }

    @GetMapping("/state")
    public GameResponse getGameState(Principal principal) {
        Game game = baccaratService.getGame(principal.getName());
        return GameResponse.of(game, game.getLastResult(), "Current game state.");
    }

    @PostMapping("/reset")
    public GameResponse resetGame(Principal principal) {
        Game game = baccaratService.resetGame(principal.getName());
        return GameResponse.of(game, "Game reset", "Game has been reset.");
    }
}
