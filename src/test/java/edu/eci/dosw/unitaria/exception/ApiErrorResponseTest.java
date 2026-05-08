package edu.eci.dosw.unitaria.exception;

import edu.eci.dosw.exception.ApiErrorResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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

        LocalDateTime timestamp =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        ApiErrorResponse response =
                new ApiErrorResponse(
                        timestamp,
                        400,
                        "Bad Request",
                        "Invalid data",
                        "/accounts/register"
                );

        assertEquals(timestamp, response.getTimestamp());
        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("Invalid data", response.getMessage());
        assertEquals("/accounts/register", response.getPath());
    }

    @Test
    void of_ShouldCreateInstanceWithTimestampAndAllFields() {

        ApiErrorResponse response =
                ApiErrorResponse.of(
                        404,
                        "Not Found",
                        "Account not found",
                        "/accounts/1"
                );

        assertNotNull(response.getTimestamp());

        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Account not found", response.getMessage());
        assertEquals("/accounts/1", response.getPath());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {

        LocalDateTime timestamp =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        ApiErrorResponse response = new ApiErrorResponse();

        response.setTimestamp(timestamp);
        response.setStatus(500);
        response.setError("Internal Server Error");
        response.setMessage("Unexpected error");
        response.setPath("/auth/login");

        assertEquals(timestamp, response.getTimestamp());
        assertEquals(500, response.getStatus());
        assertEquals("Internal Server Error", response.getError());
        assertEquals("Unexpected error", response.getMessage());
        assertEquals("/auth/login", response.getPath());
    }
}