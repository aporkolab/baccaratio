package com.prokey.baccaratio.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A single player's Baccarat table. Instances are shared between requests of the
 * same user, so all state-changing operations are synchronized.
 */
public class Game {
    private static final int RESHUFFLE_THRESHOLD = 15;

    private final Deck deck;
    private final Player player;
    private String lastResult = "";
    private BetType betType;
    private int betAmount = 0;
    private final List<Card> playerCards = new ArrayList<>();
    private final List<Card> bankerCards = new ArrayList<>();

    public Game(Player player) {
        this.deck = new Deck();
        this.player = player;
    }

    public Game(Player player, Deck deck) {
        this.player = player;
        this.deck = deck;
    }

    public synchronized boolean placeBet(BetType type, int amount) {
        if (type == null || amount <= 0 || this.player.getChips() < amount) {
            return false;
        }
        this.betType = type;
        this.betAmount = amount;
        return true;
    }

    public synchronized String playRound() {
        if (this.betAmount == 0 || this.betType == null) {
            throw new IllegalStateException("A bet must be placed before starting the round.");
        }
        if (this.player.getChips() < this.betAmount) {
            throw new IllegalStateException("Insufficient chips for this bet.");
        }

        // Deduct chips at the start of the round
        this.player.lose(this.betAmount);

        resetRoundState();
        drawInitialCards();

        int playerTotal = calculateTotal(playerCards);
        int bankerTotal = calculateTotal(bankerCards);

        if (playerTotal < 8 && bankerTotal < 8) { // No natural: apply the third card rules
            drawAdditionalCards(playerTotal);
            playerTotal = calculateTotal(playerCards);
            bankerTotal = calculateTotal(bankerCards);
        }

        lastResult = determineOutcome(playerTotal, bankerTotal);
        updateChipsBasedOnResult(lastResult);

        if (this.player.getChips() <= 0) {
            lastResult += " You have run out of chips.";
        }

        return lastResult;
    }

    private void resetRoundState() {
        playerCards.clear();
        bankerCards.clear();
        if (deck.size() < RESHUFFLE_THRESHOLD) { // Reshuffle if deck is low
            deck.reshuffle();
        }
    }

    private void drawInitialCards() {
        playerCards.add(deck.draw());
        bankerCards.add(deck.draw());
        playerCards.add(deck.draw());
        bankerCards.add(deck.draw());
    }

    private int calculateTotal(List<Card> cards) {
        return cards.stream().mapToInt(Card::points).sum() % 10;
    }

    private void drawAdditionalCards(int playerTotal) {
        boolean playerDrew = false;
        if (playerTotal <= 5) {
            playerCards.add(deck.draw());
            playerDrew = true;
        }

        int newBankerTotal = calculateTotal(bankerCards);
        if (playerDrew) {
            // Player drew, banker's draw depends on more complex rules
            int playerThirdCardValue = playerCards.get(2).points();
            if (shouldBankerDraw(newBankerTotal, playerThirdCardValue)) {
                bankerCards.add(deck.draw());
            }
        } else { // Player stands, banker draws on 5 or less
            if (newBankerTotal <= 5) {
                bankerCards.add(deck.draw());
            }
        }
    }

    private boolean shouldBankerDraw(int bankerTotal, int playerThirdCardValue) {
        return switch (bankerTotal) {
            case 0, 1, 2 -> true;
            case 3 -> playerThirdCardValue != 8;
            case 4 -> playerThirdCardValue >= 2 && playerThirdCardValue <= 7;
            case 5 -> playerThirdCardValue >= 4 && playerThirdCardValue <= 7;
            case 6 -> playerThirdCardValue == 6 || playerThirdCardValue == 7;
            default -> false;
        };
    }

    private String determineOutcome(int playerTotal, int bankerTotal) {
        if (playerTotal == bankerTotal) {
            return "Tie";
        } else if (playerTotal > bankerTotal) {
            return "Player";
        } else {
            return "Banker";
        }
    }

    private void updateChipsBasedOnResult(String winner) {
        int payout = switch (betType) {
            case PLAYER -> "Player".equals(winner) ? betAmount * 2 // 1:1 payout
                    : "Tie".equals(winner) ? betAmount : 0; // Push
            // 0.95:1 payout, rounded down
            case BANKER -> "Banker".equals(winner) ? betAmount + betAmount * 95 / 100
                    : "Tie".equals(winner) ? betAmount : 0; // Push
            case TIE -> "Tie".equals(winner) ? betAmount * 9 : 0; // 8:1 payout
            case PLAYERPAIR -> isPair(playerCards) ? betAmount * 12 : 0; // 11:1 payout
            case BANKERPAIR -> isPair(bankerCards) ? betAmount * 12 : 0; // 11:1 payout
            case EITHERPAIR -> isPair(playerCards) || isPair(bankerCards) ? betAmount * 6 : 0; // 5:1 payout
            case PERFECTPAIRONE -> isPerfectPair(playerCards) || isPerfectPair(bankerCards) ? betAmount * 26 : 0; // 25:1 payout
        };
        if (payout > 0) {
            player.win(payout);
        }
    }

    private boolean isPair(List<Card> cards) {
        return cards.size() >= 2 && cards.get(0).value().equals(cards.get(1).value());
    }

    private boolean isPerfectPair(List<Card> cards) {
        return cards.size() >= 2 &&
               cards.get(0).value().equals(cards.get(1).value()) &&
               cards.get(0).suit().equals(cards.get(1).suit());
    }

    // Getters for state
    public synchronized GameSnapshot snapshot() {
        return new GameSnapshot(player.getChips(), betType, betAmount, lastResult, playerCards, bankerCards);
    }

    public Player getPlayer() { return player; }
    public synchronized String getLastResult() { return lastResult; }
    public synchronized BetType getBetType() { return betType; }
    public synchronized int getBetAmount() { return betAmount; }
    public synchronized List<Card> getPlayerCards() { return new ArrayList<>(playerCards); }
    public synchronized List<Card> getBankerCards() { return new ArrayList<>(bankerCards); }
}
