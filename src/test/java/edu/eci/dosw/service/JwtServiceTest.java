package edu.eci.dosw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String secret;

    @BeforeEach
    void setUp() {
        String rawSecret = "12345678901234567890123456789012";
        secret = Base64.getEncoder().encodeToString(rawSecret.getBytes(StandardCharsets.UTF_8));

        jwtService = new JwtService(secret, 60000L, 120000L);
    }

    @Test
    void generateAccessToken_ShouldCreateValidAccessToken() {
        // Arrange
        Long userId = 1L;
        List<String> roles = List.of("PLAYER", "ADMIN");
        List<String> permissions = List.of("READ", "WRITE");

        // Act
        String token = jwtService.generateAccessToken(userId, roles, permissions);

        // Assert
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertFalse(jwtService.isRefreshTokenValid(token));

        assertEquals(1L, jwtService.extractUserId(token));
        assertEquals("ACCESS", jwtService.extractTokenType(token));
        assertEquals(roles, jwtService.extractRoles(token));
        assertEquals(permissions, jwtService.extractPermissions(token));
    }

    @Test
    void generateAccessToken_ShouldUseEmptyLists_WhenRolesAndPermissionsAreNull() {
        // Act
        String token = jwtService.generateAccessToken(1L, null, null);

        // Assert
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals(List.of(), jwtService.extractRoles(token));
        assertEquals(List.of(), jwtService.extractPermissions(token));
    }

    @Test
    void generateRefreshToken_ShouldCreateValidRefreshToken() {
        // Act
        String token = jwtService.generateRefreshToken(1L);

        // Assert
        assertNotNull(token);
        assertTrue(jwtService.isRefreshTokenValid(token));
        assertFalse(jwtService.isTokenValid(token));

        assertEquals(1L, jwtService.extractUserId(token));
        assertEquals("REFRESH", jwtService.extractTokenType(token));
        assertEquals(List.of(), jwtService.extractRoles(token));
        assertEquals(List.of(), jwtService.extractPermissions(token));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsMalformed() {
        // Arrange
        String invalidToken = "esto.no.es.un.jwt.valido";

        // Act
        boolean result = jwtService.isTokenValid(invalidToken);

        // Assert
        assertFalse(result);
    }

    @Test
    void isRefreshTokenValid_ShouldReturnFalse_WhenTokenIsMalformed() {
        // Arrange
        String invalidToken = "token-invalido";

        // Act
        boolean result = jwtService.isRefreshTokenValid(invalidToken);

        // Assert
        assertFalse(result);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsRefreshToken() {
        // Arrange
        String refreshToken = jwtService.generateRefreshToken(1L);

        // Act
        boolean result = jwtService.isTokenValid(refreshToken);

        // Assert
        assertFalse(result);
    }

    @Test
    void isRefreshTokenValid_ShouldReturnFalse_WhenTokenIsAccessToken() {
        // Arrange
        String accessToken = jwtService.generateAccessToken(1L, List.of("PLAYER"), List.of("READ"));

        // Act
        boolean result = jwtService.isRefreshTokenValid(accessToken);

        // Assert
        assertFalse(result);
    }

    @Test
    void token_ShouldExpire_WhenExpirationTimePasses() throws InterruptedException {
        // Arrange
        JwtService shortLivedJwtService = new JwtService(secret, 5L, 5L);
        String token = shortLivedJwtService.generateAccessToken(1L, List.of("PLAYER"), List.of("READ"));

        // Espera a que expire
        Thread.sleep(50);

        // Act
        boolean result = shortLivedJwtService.isTokenValid(token);

        // Assert
        assertFalse(result);
    }

    @Test
    void getAccessTokenExpiration_ShouldReturnConfiguredValue() {
        assertEquals(60000L, jwtService.getAccessTokenExpiration());
    }

    @Test
    void getRefreshTokenExpiration_ShouldReturnConfiguredValue() {
        assertEquals(120000L, jwtService.getRefreshTokenExpiration());
    }
}