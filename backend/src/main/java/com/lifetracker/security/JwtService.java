package com.lifetracker.security;

import com.lifetracker.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final AppProperties appProperties;

    public String createToken(String userId, String email, String name) {
        Instant now = Instant.now();
        Instant expiry = now.plus(appProperties.jwt().expirationDays(), ChronoUnit.DAYS);
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("name", name)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey())
                .compact();
    }

    public Optional<UserPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new UserPrincipal(
                    claims.getSubject(),
                    claims.get("email", String.class),
                    claims.get("name", String.class)
            ));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private SecretKey signingKey() {
        byte[] keyBytes = appProperties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
