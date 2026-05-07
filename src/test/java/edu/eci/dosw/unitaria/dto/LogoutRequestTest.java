package edu.eci.dosw.unitaria.dto;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.dto.LogoutRequest;

import static org.junit.jupiter.api.Assertions.*;

class LogoutRequestTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithNullFields() {
        LogoutRequest request = new LogoutRequest();
        assertNull(request.getRefreshToken());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        assertEquals("refresh-token", request.getRefreshToken());
    }
}