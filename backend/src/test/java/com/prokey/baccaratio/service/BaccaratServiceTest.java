package com.prokey.baccaratio.service;

import com.prokey.baccaratio.model.BetType;
import com.prokey.baccaratio.model.Game;
import com.prokey.baccaratio.model.Player;
import com.prokey.baccaratio.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaccaratServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    private BaccaratService baccaratService;

    private Player player;

    @BeforeEach
    void setUp() {
        baccaratService = new BaccaratService(playerRepository, 1000);
        player = new Player("testuser", 1000);
        lenient().when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void getOrCreatePlayer_FindsExistingPlayer() {
        when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));
        Player foundPlayer = baccaratService.getOrCreatePlayer("testuser");
        assertEquals("testuser", foundPlayer.getName());
        verify(playerRepository).findByName("testuser");
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void getOrCreatePlayer_CreatesNewPlayer() {
        when(playerRepository.findByName("newuser")).thenReturn(Optional.empty());

        Player newPlayer = baccaratService.getOrCreatePlayer("newuser");

        assertEquals("newuser", newPlayer.getName());
        assertEquals(1000, newPlayer.getChips());
        verify(playerRepository).findByName("newuser");
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void getGame_ReusesTheSameGamePerUserAndSeparatesUsers() {
        when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));
        when(playerRepository.findByName("other")).thenReturn(Optional.of(new Player("other", 1000)));

        Game first = baccaratService.getGame("testuser");

        assertSame(first, baccaratService.getGame("testuser"));
        assertNotSame(first, baccaratService.getGame("other"));
        verify(playerRepository, times(1)).findByName("testuser");
    }

    @Test
    void placeBet_StoresTheBetOnTheUsersGame() {
        when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));

        Game game = baccaratService.placeBet("testuser", "playerPair", 50);

        assertEquals(BetType.PLAYERPAIR, game.getBetType());
        assertEquals(50, game.getBetAmount());
    }

    @Test
    void placeBet_RejectsInvalidInput() {
        lenient().when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));

        assertThrows(IllegalArgumentException.class, () -> baccaratService.placeBet("testuser", "player", 0));
        assertThrows(IllegalArgumentException.class, () -> baccaratService.placeBet("testuser", "dragon", 10));
        assertThrows(IllegalArgumentException.class, () -> baccaratService.placeBet("testuser", "player", 1001));
    }

    @Test
    void playRound_PersistsTheNewBalance() {
        when(playerRepository.findByName("testuser")).thenReturn(Optional.of(player));
        baccaratService.placeBet("testuser", "tie", 100);

        Game game = baccaratService.playRound("testuser");

        assertTrue(game.getPlayerCards().size() >= 2);
        assertTrue(game.getBankerCards().size() >= 2);
        assertFalse(game.getLastResult().isEmpty());
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
        Game before = baccaratService.getGame("testuser");
        player.setChips(10);

        Game after = baccaratService.resetGame("testuser");

        assertNotSame(before, after);
        assertEquals(1000, after.getPlayer().getChips());
        assertEquals(0, after.getBetAmount());
        verify(playerRepository).save(player);
    }
}
