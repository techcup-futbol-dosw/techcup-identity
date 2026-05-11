package edu.eci.dosw.unitary.dto;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.dto.AuthRequest;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithNullFields() {
        AuthRequest request = new AuthRequest();
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        AuthRequest request = new AuthRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");

        assertEquals("juan@escuelaing.edu.co", request.getEmail());
        assertEquals("123456", request.getPassword());
    }
}