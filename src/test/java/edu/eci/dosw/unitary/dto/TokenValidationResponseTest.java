package edu.eci.dosw.unitary.dto;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.dto.TokenValidationResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenValidationResponseTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithDefaultValues() {
        TokenValidationResponse response = new TokenValidationResponse();
        assertFalse(response.isValid());
        assertNull(response.getAccountId());
        assertNull(response.getRoles());
        assertNull(response.getPermissions());
        assertNull(response.getTokenType());
    }

    @Test
    void parameterizedConstructor_ShouldSetAllFields() {
        TokenValidationResponse response = new TokenValidationResponse(
                true, 1L, List.of("PLAYER"), List.of("tournament:read"), "Bearer");

        assertTrue(response.isValid());
        assertEquals(1L, response.getAccountId());
        assertEquals(List.of("PLAYER"), response.getRoles());
        assertEquals(List.of("tournament:read"), response.getPermissions());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        TokenValidationResponse response = new TokenValidationResponse();
        response.setValid(true);
        response.setAccountId(1L);
        response.setRoles(List.of("ADMIN"));
        response.setPermissions(List.of("account:read"));
        response.setTokenType("Bearer");

        assertTrue(response.isValid());
        assertEquals(1L, response.getAccountId());
        assertEquals(List.of("ADMIN"), response.getRoles());
        assertEquals(List.of("account:read"), response.getPermissions());
        assertEquals("Bearer", response.getTokenType());
    }
}