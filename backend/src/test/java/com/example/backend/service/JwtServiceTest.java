package com.example.backend.service;

import com.example.backend.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "test-jwt-secret-at-least-32-characters-long";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60);
    }

    private AppUser sampleUser() {
        AppUser user = new AppUser("Ada Lovelace", "ada@example.com", "hashed", "STUDENT");
        user.setId("user-1");
        return user;
    }

    @Test
    void generatedTokenRoundTripsThroughParseTokenWithExpectedClaims() {
        String token = jwtService.generateToken(sampleUser());

        Claims claims = jwtService.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("ada@example.com");
        assertThat(claims.get("userId", String.class)).isEqualTo("user-1");
        assertThat(claims.get("name", String.class)).isEqualTo("Ada Lovelace");
        assertThat(claims.get("role", String.class)).isEqualTo("STUDENT");
    }

    @Test
    void expirationMatchesConfiguredMinutes() {
        String token = jwtService.generateToken(sampleUser());
        Claims claims = jwtService.parseToken(token);

        long actualMinutes = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 60_000;

        assertThat(actualMinutes).isEqualTo(60L);
        assertThat(jwtService.getExpirationMinutes()).isEqualTo(60L);
    }

    @Test
    void tokensSignedWithADifferentSecretFailToParse() {
        String token = jwtService.generateToken(sampleUser());
        JwtService otherService = new JwtService("a-completely-different-secret-that-is-also-long-enough", 60);

        assertThat(token).isNotBlank();
        assertThrows(SignatureException.class, () -> otherService.parseToken(token));
    }
}
