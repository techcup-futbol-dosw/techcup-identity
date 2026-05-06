package edu.eci.dosw.unitaria.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.eci.dosw.security.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AccessDeniedHandlerImplTest {

    private final AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();

    @Test
    void handle_ShouldReturn403_WithCorrectBody() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/roles/assign");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals("application/json", response.getContentType());

        Map<?, ?> body = new ObjectMapper().readValue(response.getContentAsString(), Map.class);
        assertEquals(403, body.get("status"));
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Don't have permissions for this resource", body.get("message"));
        assertEquals("/roles/assign", body.get("path"));
        assertNotNull(body.get("timestamp"));
    }
}