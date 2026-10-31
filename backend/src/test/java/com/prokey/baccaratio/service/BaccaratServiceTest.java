package com.prokey.baccaratio.service;

import com.prokey.baccaratio.model.BetType;
import com.prokey.baccaratio.model.GameSnapshot;
import com.prokey.baccaratio.model.Player;
import com.prokey.baccaratio.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaccaratServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    private BaccaratService baccaratService;

    private Player player;

    @BeforeEach
    void setUp() {
        baccaratService = new BaccaratService(playerRepository, transactionManager, 1000);
        player = new Player("testuser", 1000);
        lenient().when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(transactionManager.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
    }

    @Test
    void getGame_FindsExistingPlayer() {
        when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));
        GameSnapshot game = baccaratService.getGame("testuser");
        assertEquals(1000, game.playerChips());
        verify(playerRepository).findByName("testuser");
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void getGame_CreatesNewPlayer() {
        when(playerRepository.findByName("newuser")).thenReturn(Optional.empty());

        GameSnapshot game = baccaratService.getGame("newuser");

        assertEquals(1000, game.playerChips());
        verify(playerRepository).findByName("newuser");
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void getGame_KeepsBetsBetweenRequestsAndSeparatesUsers() {
        when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));
        when(playerRepository.findByName("other")).thenReturn(Optional.of(new Player("other", 1000)));

        baccaratService.placeBet("testuser", "banker", 25);

        assertEquals(25, baccaratService.getGame("testuser").betAmount());
        assertEquals(BetType.BANKER, baccaratService.getGame("testuser").betType());
        assertEquals(0, baccaratService.getGame("other").betAmount());
        verify(playerRepository, times(1)).findByName("testuser");
    }

    @Test
    void placeBet_StoresTheBetOnTheUsersGame() {
        when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));

        GameSnapshot game = baccaratService.placeBet("testuser", "playerPair", 50);

        assertEquals(BetType.PLAYERPAIR, game.betType());
        assertEquals(50, game.betAmount());
    }

    @Test
    void placeBet_RejectsInvalidInput() {
        lenient().when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));

        assertThrows(IllegalArgumentException.class, () -> baccaratService.placeBet("testuser", "player", 0));
        assertThrows(IllegalArgumentException.class, () -> baccaratService.placeBet("testuser", "dragon", 10));
        assertThrows(IllegalArgumentException.class, () -> baccaratService.placeBet("testuser", "player", 1001));
    }

    @Test
    void rejectedBet_DoesNotDiscardThePreviousBet() {
        when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));
        GameSnapshot before = baccaratService.placeBet("testuser", "player", 25);

        assertThrows(IllegalArgumentException.class, () -> baccaratService.placeBet("testuser", "banker", 1001));

        assertEquals(before, baccaratService.getGame("testuser"));
        verify(playerRepository).findByName("testuser");
    }

    @Test
    void playRound_PersistsTheNewBalance() {
        when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));
        baccaratService.placeBet("testuser", "tie", 100);

        GameSnapshot game = baccaratService.playRound("testuser");

        assertTrue(game.playerCards().size() >= 2);
        assertTrue(game.bankerCards().size() >= 2);
        assertFalse(game.lastResult().isEmpty());
        verify(playerRepository).save(player);
    }

    @Test
    void playRound_WithoutBetFails() {
        when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));

        assertThrows(IllegalStateException.class, () -> baccaratService.playRound("testuser"));
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void resetGame_RestoresInitialChipsAndStartsAFreshGame() {
        when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));
        baccaratService.placeBet("testuser", "player", 10);
        GameSnapshot before = baccaratService.playRound("testuser");
        player.setChips(10);

        GameSnapshot after = baccaratService.resetGame("testuser");

        assertEquals(1000, after.playerChips());
        assertEquals(0, after.betAmount());
        assertNull(after.betType());
        assertEquals("", after.lastResult());
        assertTrue(after.playerCards().isEmpty());
        assertTrue(after.bankerCards().isEmpty());
        assertTrue(before.playerCards().size() >= 2);
        assertFalse(before.lastResult().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> before.playerCards().clear());
        verify(playerRepository, times(2)).save(player);
    }

    @Test
    void failedSave_DiscardsMutatedMemoryAndReloadsThePersistedBalance() {
        when(playerRepository.findByName("testuser"))
                .thenReturn(Optional.of(player), Optional.of(new Player("testuser", 1000)));
        baccaratService.placeBet("testuser", "tie", 100);
        when(playerRepository.save(player)).thenThrow(new DataAccessResourceFailureException("database unavailable"));

        assertThrows(DataAccessResourceFailureException.class, () -> baccaratService.playRound("testuser"));

        GameSnapshot reloaded = baccaratService.getGame("testuser");
        assertEquals(1000, reloaded.playerChips());
        assertEquals(0, reloaded.betAmount());
        assertTrue(reloaded.playerCards().isEmpty());
        verify(playerRepository, times(2)).findByName("testuser");
    }

    @Test
    void failedCommit_DiscardsMutatedMemoryAndReloadsThePersistedBalance() {
        when(playerRepository.findByName("testuser"))
                .thenReturn(Optional.of(player), Optional.of(new Player("testuser", 1000)));
        baccaratService.placeBet("testuser", "tie", 100);
        doThrow(new TransactionSystemException("commit failed")).doNothing().when(transactionManager).commit(any());

        assertThrows(TransactionSystemException.class, () -> baccaratService.playRound("testuser"));

        GameSnapshot reloaded = baccaratService.getGame("testuser");
        assertEquals(1000, reloaded.playerChips());
        assertEquals(0, reloaded.betAmount());
        assertTrue(reloaded.playerCards().isEmpty());
        verify(playerRepository, times(2)).findByName("testuser");
    }
}
