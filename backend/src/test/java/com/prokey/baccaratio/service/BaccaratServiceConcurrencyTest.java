package com.prokey.baccaratio.service;

import com.prokey.baccaratio.model.GameSnapshot;
import com.prokey.baccaratio.model.Player;
import com.prokey.baccaratio.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Timeout(20)
class BaccaratServiceConcurrencyTest {
    @Mock private PlayerRepository repository;
    @Mock private PlatformTransactionManager transactionManager;
    private BaccaratService service;

    @BeforeEach
    void setUp() {
        service = new BaccaratService(repository, transactionManager, 1000);
        when(transactionManager.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
        lenient().when(repository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, SECONDS), "Timed out waiting for the controlled concurrent operation");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }

    @Test
    void firstPlayerCreationWaitsForCommitAndDoesNotBlockAnotherUser() throws Exception {
        CountDownLatch committing = new CountDownLatch(1);
        CountDownLatch allowCommit = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        AtomicInteger commits = new AtomicInteger();
        doAnswer(invocation -> {
            if (commits.incrementAndGet() == 1) {
                committing.countDown();
                await(allowCommit);
            }
            return null;
        }).when(transactionManager).commit(any());

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> service.getGame("newuser"));
            try {
                await(committing);
                var second = executor.submit(() -> {
                    secondStarted.countDown();
                    return service.getGame("newuser");
                });
                await(secondStarted);
                assertThrows(TimeoutException.class, () -> second.get(150, MILLISECONDS));
                assertEquals(1000, service.getGame("other").playerChips());
                assertFalse(first.isDone());
                allowCommit.countDown();
                assertEquals(first.get(5, SECONDS), second.get(5, SECONDS));
            } finally {
                allowCommit.countDown();
            }
        }
        verify(repository).findByName("newuser");
        verify(repository).save(argThat(player -> player.getName().equals("newuser")));
    }

    @Test
    void resetWaitsForAnInFlightRoundAndCannotChangeItsResponse() throws Exception {
        Player player = new Player("testuser", 1000);
        when(repository.findByName("testuser")).thenReturn(Optional.of(player));
        service.placeBet("testuser", "tie", 100);
        CountDownLatch saving = new CountDownLatch(1);
        CountDownLatch allowSave = new CountDownLatch(1);
        CountDownLatch resetStarted = new CountDownLatch(1);
        AtomicInteger saves = new AtomicInteger();
        when(repository.save(player)).thenAnswer(invocation -> {
            if (saves.incrementAndGet() == 1) {
                saving.countDown();
                await(allowSave);
            }
            return player;
        });

        try (var executor = Executors.newFixedThreadPool(2)) {
            var round = executor.submit(() -> service.playRound("testuser"));
            try {
                await(saving);
                var reset = executor.submit(() -> {
                    resetStarted.countDown();
                    return service.resetGame("testuser");
                });
                await(resetStarted);
                assertThrows(TimeoutException.class, () -> reset.get(150, MILLISECONDS));
                allowSave.countDown();
                GameSnapshot played = round.get(5, SECONDS);
                GameSnapshot cleared = reset.get(5, SECONDS);
                assertEquals(1000, cleared.playerChips());
                assertEquals(0, cleared.betAmount());
                assertTrue(cleared.playerCards().isEmpty());
                assertFalse(played.lastResult().isEmpty());
                assertTrue(played.playerCards().size() >= 2);
                assertEquals(played.lastResult().equals("Tie") ? 1800 : 900, played.playerChips());
                assertEquals(cleared, service.getGame("testuser"));
            } finally {
                allowSave.countDown();
            }
        }
        verify(repository, times(2)).save(player);
    }
}
