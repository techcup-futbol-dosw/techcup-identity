package edu.eci.dosw.unitaria.dto;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.dto.RefreshTokenRequest;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenRequestTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithNullFields() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        assertNull(request.getRefreshToken());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        assertEquals("refresh-token", request.getRefreshToken());
    }
}