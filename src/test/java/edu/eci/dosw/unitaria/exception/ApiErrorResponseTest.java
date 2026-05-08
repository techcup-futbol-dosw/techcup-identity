package edu.eci.dosw.unitaria.exception;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.exception.ApiErrorResponse;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorResponseTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithDefaultValues() {
        ApiErrorResponse response = new ApiErrorResponse();
        assertNull(response.getTimestamp());
        assertEquals(0, response.getStatus());
        assertNull(response.getError());
        assertNull(response.getMessage());
        assertNull(response.getPath());
    }

    @Test
    void parameterizedConstructor_ShouldSetAllFields() {
        ApiErrorResponse response = new ApiErrorResponse("2026-01-01T00:00:00", 400, "Bad Request", "Invalid data", "/accounts/register");

        assertEquals("2026-01-01T00:00:00", response.getTimestamp());
        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("Invalid data", response.getMessage());
        assertEquals("/accounts/register", response.getPath());
    }

    @Test
    void of_ShouldCreateInstanceWithTimestampAndAllFields() {
        ApiErrorResponse response = ApiErrorResponse.of(404, "Not Found", "Account not found", "/accounts/1");

        assertNotNull(response.getTimestamp());
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Account not found", response.getMessage());
        assertEquals("/accounts/1", response.getPath());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        ApiErrorResponse response = new ApiErrorResponse();
        response.setTimestamp("2026-01-01T00:00:00");
        response.setStatus(500);
        response.setError("Internal Server Error");
        response.setMessage("Unexpected error");
        response.setPath("/auth/login");

        assertEquals("2026-01-01T00:00:00", response.getTimestamp());
        assertEquals(500, response.getStatus());
        assertEquals("Internal Server Error", response.getError());
        assertEquals("Unexpected error", response.getMessage());
        assertEquals("/auth/login", response.getPath());
    }
}