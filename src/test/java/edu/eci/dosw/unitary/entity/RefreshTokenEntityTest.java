package edu.eci.dosw.unitary.entity;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.RefreshTokenEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenEntityTest {

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        AccountEntity account = new AccountEntity();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        token.setId(1L);
        token.setToken("refresh-token-value");
        token.setAccount(account);
        token.setRevoked(false);
        token.setExpiresAt(expiresAt);

        assertEquals(1L, token.getId());
        assertEquals("refresh-token-value", token.getToken());
        assertEquals(account, token.getAccount());
        assertFalse(token.isRevoked());
        assertEquals(expiresAt, token.getExpiresAt());
    }

    @Test
    void defaultConstructor_ShouldCreateInstanceWithDefaultValues() {
        RefreshTokenEntity token = new RefreshTokenEntity();

        assertNull(token.getId());
        assertNull(token.getToken());
        assertNull(token.getAccount());
        assertFalse(token.isRevoked());
        assertNull(token.getExpiresAt());
    }

    @Test
    void isExpired_ShouldReturnTrue_WhenExpiresAtIsInThePast() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setExpiresAt(LocalDateTime.now().minusSeconds(1));

        assertTrue(token.isExpired());
    }

    @Test
    void isExpired_ShouldReturnFalse_WhenExpiresAtIsInTheFuture() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setExpiresAt(LocalDateTime.now().plusDays(7));

        assertFalse(token.isExpired());
    }

    @Test
    void setRevoked_ShouldUpdateRevokedState() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setRevoked(true);

        assertTrue(token.isRevoked());
    }
}