package com.prokey.baccaratio.service;

import com.prokey.baccaratio.model.BetType;
import com.prokey.baccaratio.model.Game;
import com.prokey.baccaratio.model.Player;
import com.prokey.baccaratio.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages one in-memory {@link Game} per authenticated user and persists chip balances.
 * <p>
 * Games are keyed by username rather than by HTTP session: the API is stateless (JWT),
 * so clients do not send a session cookie and a session-keyed game would be lost
 * between placing a bet and playing the round.
 */
@Service
public class BaccaratService {

    private final Map<String, Game> activeGames = new ConcurrentHashMap<>();
    private final PlayerRepository playerRepository;
    private final int initialChips;

    public BaccaratService(PlayerRepository playerRepository,
                           @Value("${app.game.initial-chips:1000}") int initialChips) {
        this.playerRepository = playerRepository;
        this.initialChips = initialChips;
    }

    @Transactional
    public Player getOrCreatePlayer(String username) {
        return playerRepository.findByName(username)
                .orElseGet(() -> playerRepository.save(new Player(username, initialChips)));
    }

    @Transactional
    public Game getGame(String username) {
        Game game = activeGames.get(username);
        if (game != null) {
            return game;
        }
        Player player = getOrCreatePlayer(username);
        return activeGames.computeIfAbsent(username, name -> new Game(player));
    }

    @Transactional
    public Game placeBet(String username, String type, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive.");
        }
        BetType betType = BetType.parse(type);
        Game game = getGame(username);
        if (!game.placeBet(betType, amount)) {
            throw new IllegalArgumentException("Insufficient chips for this bet.");
        }
        return game;
    }

    @Transactional
    public Game playRound(String username) {
        Game game = getGame(username);
        synchronized (game) {
            game.playRound();
            playerRepository.save(game.getPlayer());
        }
        return game;
    }

    @Transactional
    public Game resetGame(String username) {
        Player player = getGame(username).getPlayer();
        player.setChips(initialChips);
        playerRepository.save(player);
        activeGames.remove(username);
        return getGame(username);
    }

    public int getInitialChips() {
        return initialChips;
    }
}
