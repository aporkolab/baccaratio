package com.prokey.baccaratio.service;

import com.prokey.baccaratio.model.BetType;
import com.prokey.baccaratio.model.Game;
import com.prokey.baccaratio.model.GameSnapshot;
import com.prokey.baccaratio.model.Player;
import com.prokey.baccaratio.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

/**
 * Manages one in-memory {@link Game} per authenticated user and persists chip balances.
 * <p>
 * Games are keyed by username rather than by HTTP session: the API is stateless (JWT),
 * so clients do not send a session cookie and a session-keyed game would be lost
 * between placing a bet and playing the round.
 */
@Service
public class BaccaratService {

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final PlayerRepository playerRepository;
    private final TransactionTemplate transaction;
    private final int initialChips;

    public BaccaratService(PlayerRepository playerRepository,
                           PlatformTransactionManager transactionManager,
                           @Value("${app.game.initial-chips:1000}") int initialChips) {
        this.playerRepository = playerRepository;
        this.initialChips = initialChips;
        this.transaction = new TransactionTemplate(transactionManager);
        // The per-user lock must cover the actual commit, even if a caller
        // already has a transaction. @Transactional would commit after unlock.
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public GameSnapshot getGame(String username) {
        return updateGame(username, UnaryOperator.identity());
    }

    public GameSnapshot placeBet(String username, String type, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive.");
        }
        BetType betType = BetType.parse(type);
        return updateGame(username, game -> {
            if (!game.placeBet(betType, amount)) {
                throw new IllegalArgumentException("Insufficient chips for this bet.");
            }
            return game;
        });
    }

    public GameSnapshot playRound(String username) {
        return updateGame(username, game -> {
            game.playRound();
            playerRepository.save(game.getPlayer());
            return game;
        });
    }

    public GameSnapshot resetGame(String username) {
        return updateGame(username, game -> {
            Player player = game.getPlayer();
            player.setChips(initialChips);
            playerRepository.save(player);
            return new Game(player);
        });
    }

    private GameSnapshot updateGame(String username, UnaryOperator<Game> operation) {
        // Only allocate the lock inside computeIfAbsent; unrelated users never
        // wait on database work under a ConcurrentHashMap bucket lock.
        GameSession session = sessions.computeIfAbsent(username, ignored -> new GameSession());
        synchronized (session) {
            try {
                GameUpdate update = transaction.execute(status -> {
                    Game game = session.game;
                    if (game == null) {
                        Player player = playerRepository.findByName(username)
                                .orElseGet(() -> playerRepository.save(new Player(username, initialChips)));
                        game = new Game(player);
                    }
                    Game updated = operation.apply(game);
                    return new GameUpdate(updated, updated.snapshot());
                });
                // Publish new games and reset replacements only after commit.
                session.game = update.game();
                return update.snapshot();
            } catch (DataAccessException | TransactionException exception) {
                // A failed save/commit can leave the detached in-memory player
                // mutated. Reload the persisted balance on the next request.
                session.game = null;
                throw exception;
            }
        }
    }

    private static final class GameSession {
        private Game game;
    }

    private record GameUpdate(Game game, GameSnapshot snapshot) {
    }

    public int getInitialChips() {
        return initialChips;
    }
}
