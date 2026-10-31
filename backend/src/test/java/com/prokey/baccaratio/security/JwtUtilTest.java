package com.prokey.baccaratio.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    private final UserDetails alice = new User("alice", "x", List.of());
    private final UserDetails bob = new User("bob", "x", List.of());

    private static JwtUtil jwtUtilAt(Instant instant) {
        return new JwtUtil(SECRET, Duration.ofHours(1), Clock.fixed(instant, ZoneOffset.UTC));
    }

    @Test
    void generatedTokenIsValidForItsUser() {
        JwtUtil jwtUtil = jwtUtilAt(NOW);
        String token = jwtUtil.generateToken(alice);

        assertEquals("alice", jwtUtil.extractUsername(token));
        assertTrue(jwtUtil.validateToken(token, alice));
        assertFalse(jwtUtil.validateToken(token, bob));
    }

    @Test
    void expiredTokenIsRejected() {
        String token = jwtUtilAt(NOW).generateToken(alice);
        JwtUtil later = jwtUtilAt(NOW.plus(Duration.ofHours(2)));

        assertThrows(JwtException.class, () -> later.extractUsername(token));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String foreignSecret = Base64.getEncoder().encodeToString("another-secret-key-of-32-bytes!!".getBytes());
        String token = new JwtUtil(foreignSecret, Duration.ofHours(1)).generateToken(alice);

        assertThrows(JwtException.class, () -> jwtUtilAt(Instant.now()).extractUsername(token));
    }

    @Test
    void malformedTokenIsRejected() {
        assertThrows(JwtException.class, () -> jwtUtilAt(NOW).extractUsername("not-a-jwt"));
    }

    @Test
    void missingSecretFallsBackToARandomKeyPerInstance() {
        JwtUtil first = new JwtUtil("", Duration.ofHours(1));
        JwtUtil second = new JwtUtil("", Duration.ofHours(1));
        String token = first.generateToken(alice);

        assertEquals("alice", first.extractUsername(token));
        assertThrows(JwtException.class, () -> second.extractUsername(token));
    }
}
