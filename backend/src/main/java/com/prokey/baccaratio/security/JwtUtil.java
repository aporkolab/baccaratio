package com.prokey.baccaratio.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;

/**
 * Issues and validates HMAC-SHA signed JWTs.
 * <p>
 * The signing key comes from {@code jwt.secret} (Base64, at least 256 bits). When it is not set, a random
 * key is generated at startup: tokens then do not survive a restart, which matches the in-memory database.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey secretKey;
    private final Duration expiration;
    private final Clock clock;

    @Autowired
    public JwtUtil(@Value("${jwt.secret:}") String secret,
                   @Value("${jwt.expiration:PT10H}") Duration expiration) {
        this(secret, expiration, Clock.systemUTC());
    }

    JwtUtil(String secret, Duration expiration, Clock clock) {
        if (secret == null || secret.isBlank()) {
            log.warn("jwt.secret (JWT_SECRET) is not set; using a random signing key. Issued tokens will not survive a restart.");
            this.secretKey = Jwts.SIG.HS256.key().build();
        } else {
            this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        }
        this.expiration = expiration;
        this.clock = clock;
    }

    /**
     * Returns the subject of a validly signed, unexpired token.
     *
     * @throws io.jsonwebtoken.JwtException if the token is malformed, tampered with or expired
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(UserDetails userDetails) {
        Date now = Date.from(clock.instant());
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration.toMillis()))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Checks that the token is validly signed, unexpired and belongs to the given user.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        return userDetails.getUsername().equals(claims.getSubject());
    }
}
