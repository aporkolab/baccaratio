package com.prokey.baccaratio.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameTest {

    private Player player;

    @Mock
    private Deck deck;

    @BeforeEach
    void setUp() {
        player = new Player("testuser", 1000);
    }

    /** Cards are dealt player, banker, player, banker, then third cards (player first). */
    private Game gameDealing(Card first, Card... rest) {
        when(deck.draw()).thenReturn(first, rest);
        return new Game(player, deck);
    }

    private static Card card(String value, int points) {
        return new Card("Hearts", value, points);
    }

    private static Card card(String suit, String value, int points) {
        return new Card(suit, value, points);
    }

    @Test
    void testPlayerPairWin() {
        Game game = gameDealing(card("Hearts", "A", 1), card("Diamonds", "2", 2),
                card("Clubs", "A", 1), card("Spades", "3", 3));
        game.placeBet(BetType.PLAYERPAIR, 100);
        game.playRound();
        assertEquals(2100, player.getChips());
    }

    @Test
    void testBankerPairWin() {
        Game game = gameDealing(card("Hearts", "2", 2), card("Clubs", "A", 1),
                card("Diamonds", "3", 3), card("Spades", "A", 1));
        game.placeBet(BetType.BANKERPAIR, 100);
        game.playRound();
        assertEquals(2100, player.getChips());
    }

    @Test
    void testEitherPairWin() {
        Game game = gameDealing(card("Hearts", "A", 1), card("Diamonds", "2", 2),
                card("Clubs", "A", 1), card("Spades", "3", 3));
        game.placeBet(BetType.EITHERPAIR, 100);
        game.playRound();
        assertEquals(1500, player.getChips());
    }

    @Test
    void testPerfectPairWin() {
        Game game = gameDealing(card("Hearts", "A", 1), card("Clubs", "2", 2),
                card("Hearts", "A", 1), card("Spades", "3", 3));
        game.placeBet(BetType.PERFECTPAIRONE, 100);
        game.playRound();
        assertEquals(3500, player.getChips());
    }

    @Test
    void naturalPlayerWinPaysEvenMoneyWithoutThirdCards() {
        // Player 4+5 = 9 (natural), banker 2+3 = 5
        Game game = gameDealing(card("4", 4), card("2", 2), card("5", 5), card("3", 3));
        game.placeBet(BetType.PLAYER, 100);

        assertEquals("Player", game.playRound());
        assertEquals(1100, player.getChips());
        assertEquals(2, game.getPlayerCards().size());
        assertEquals(2, game.getBankerCards().size());
    }

    @Test
    void bankerWinPaysNinetyFivePercentRoundedDown() {
        // Player 2+5 = 7 stands, banker 4+4 = 8 natural
        Game game = gameDealing(card("2", 2), card("4", 4), card("5", 5), card("4", 4));
        game.placeBet(BetType.BANKER, 30);

        assertEquals("Banker", game.playRound());
        // 30 + floor(30 * 0.95) = 30 + 28 = 58
        assertEquals(1000 - 30 + 58, player.getChips());
    }

    @Test
    void bankerWinPaysNinetyFivePercentOnRoundAmounts() {
        Game game = gameDealing(card("2", 2), card("4", 4), card("5", 5), card("4", 4));
        game.placeBet(BetType.BANKER, 100);

        game.playRound();

        assertEquals(1000 - 100 + 195, player.getChips());
    }

    @Test
    void tieReturnsPlayerBetAsPush() {
        // Player 4+4 = 8, banker 3+5 = 8 (both natural)
        Game game = gameDealing(card("4", 4), card("3", 3), card("4", 4), card("5", 5));
        game.placeBet(BetType.PLAYER, 100);

        assertEquals("Tie", game.playRound());
        assertEquals(1000, player.getChips());
    }

    @Test
    void tieBetPaysEightToOne() {
        Game game = gameDealing(card("4", 4), card("3", 3), card("4", 4), card("5", 5));
        game.placeBet(BetType.TIE, 10);

        game.playRound();

        assertEquals(1000 - 10 + 90, player.getChips());
    }

    @Test
    void losingBetDeductsStake() {
        Game game = gameDealing(card("2", 2), card("4", 4), card("5", 5), card("4", 4));
        game.placeBet(BetType.PLAYER, 100);

        assertEquals("Banker", game.playRound());
        assertEquals(900, player.getChips());
    }

    @Test
    void playerDrawsOnFiveOrLessAndBankerFollowsThirdCardRule() {
        // Player 2+3 = 5 draws a 4 -> 9; banker 3+3 = 6 does not draw on a player third card of 4
        Game game = gameDealing(card("2", 2), card("3", 3), card("3", 3), card("3", 3), card("4", 4));
        game.placeBet(BetType.PLAYER, 100);

        assertEquals("Player", game.playRound());
        assertEquals(3, game.getPlayerCards().size());
        assertEquals(2, game.getBankerCards().size());
    }

    @Test
    void bankerWithThreeDrawsUnlessPlayerThirdCardIsEight() {
        // Player 1+2 = 3 draws 6 -> 9; banker 1+2 = 3 draws a K (0) -> 3
        Game game = gameDealing(card("A", 1), card("A", 1), card("2", 2), card("2", 2), card("6", 6), card("K", 0));
        game.placeBet(BetType.PLAYER, 100);

        assertEquals("Player", game.playRound());
        assertEquals(3, game.getBankerCards().size());
    }

    @Test
    void bankerDrawsOnFiveOrLessWhenPlayerStands() {
        // Player 3+4 = 7 stands; banker 2+3 = 5 draws a 3 -> 8
        Game game = gameDealing(card("3", 3), card("2", 2), card("4", 4), card("3", 3), card("3", 3));
        game.placeBet(BetType.BANKER, 100);

        assertEquals("Banker", game.playRound());
        assertEquals(2, game.getPlayerCards().size());
        assertEquals(3, game.getBankerCards().size());
    }

    @Test
    void runningOutOfChipsIsReportedAlsoOnNaturals() {
        player = new Player("broke", 100);
        Game game = gameDealing(card("2", 2), card("4", 4), card("5", 5), card("4", 4));
        game.placeBet(BetType.PLAYER, 100);

        assertEquals("Banker You have run out of chips.", game.playRound());
        assertEquals(0, player.getChips());
    }

    @Test
    void playingWithoutABetIsRejected() {
        Game game = new Game(player, deck);
        assertThrows(IllegalStateException.class, game::playRound);
        assertEquals(1000, player.getChips());
    }

    @Test
    void invalidBetsAreRejected() {
        Game game = new Game(player, deck);
        assertFalse(game.placeBet(BetType.PLAYER, 0));
        assertFalse(game.placeBet(BetType.PLAYER, -5));
        assertFalse(game.placeBet(BetType.PLAYER, 1001));
        assertFalse(game.placeBet(null, 10));
        assertTrue(game.placeBet(BetType.PLAYER, 1000));
    }

    @Test
    void roundFailsWhenBalanceDroppedBelowTheStandingBet() {
        Game game = new Game(player, deck);
        game.placeBet(BetType.PLAYER, 800);
        player.setChips(500);

        assertThrows(IllegalStateException.class, game::playRound);
        assertEquals(500, player.getChips());
    }
}
