package edu.eci.dosw.unitary.dto;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.dto.TokenValidationRequest;

import static org.junit.jupiter.api.Assertions.*;

class TokenValidationRequestTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithNullFields() {
        TokenValidationRequest request = new TokenValidationRequest();
        assertNull(request.getToken());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        TokenValidationRequest request = new TokenValidationRequest();
        request.setToken("my-token");

        assertEquals("my-token", request.getToken());
    }
}