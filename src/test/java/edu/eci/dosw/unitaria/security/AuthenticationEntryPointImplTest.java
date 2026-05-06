package edu.eci.dosw.unitaria.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.eci.dosw.security.AuthenticationEntryPointImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationEntryPointImplTest {

    private final AuthenticationEntryPointImpl entryPoint = new AuthenticationEntryPointImpl();

    @Test
    void commence_ShouldReturn401_WithCorrectBody() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/accounts/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new AuthenticationException("Unauthorized") {});

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("application/json", response.getContentType());

        Map<?, ?> body = new ObjectMapper().readValue(response.getContentAsString(), Map.class);
        assertEquals(401, body.get("status"));
        assertEquals("Unauthorized", body.get("error"));
        assertEquals("Token invalido, expirado o ausente", body.get("message"));
        assertEquals("/accounts/1", body.get("path"));
        assertNotNull(body.get("timestamp"));
    }
}