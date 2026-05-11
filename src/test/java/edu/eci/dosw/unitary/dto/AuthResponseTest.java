package edu.eci.dosw.unitary.dto;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.dto.AuthResponse;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithDefaultValues() {
        AuthResponse response = new AuthResponse();
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
        assertNull(response.getTokenType());
        assertEquals(0L, response.getExpiresIn());
    }

    @Test
    void parameterizedConstructor_ShouldSetAllFields() {
        AuthResponse response = new AuthResponse("access-token", "refresh-token", "Bearer", 3600L);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        AuthResponse response = new AuthResponse();
        response.setAccessToken("access-token");
        response.setRefreshToken("refresh-token");
        response.setTokenType("Bearer");
        response.setExpiresIn(3600L);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
    }
}