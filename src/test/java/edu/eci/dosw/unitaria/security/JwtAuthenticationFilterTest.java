package edu.eci.dosw.unitaria.security;

import edu.eci.dosw.security.*;
import edu.eci.dosw.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String VALID_TOKEN = "valid-token";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String BAD_TOKEN = "bad-token";
    private static final String BASIC_TOKEN = "Basic sometoken";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final Long ACCOUNT_ID = 1L;

    private static final String PLAYER_ROLE = "PLAYER";
    private static final String PLAYER_AUTHORITY = "ROLE_PLAYER";
    private static final String TOURNAMENT_READ_PERMISSION = "tournament:read";

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldSkipFilter_WhenNoAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithoutAuthorization();
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        verifyFilterContinues(request, response);
        verifyNoInteractions(jwtService);
        assertNoAuthentication();
    }

    @Test
    void doFilterInternal_ShouldSkipFilter_WhenHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithAuthorization(BASIC_TOKEN);
        MockHttpServletResponse response = response();

        filter.doFilter(request, response, filterChain);

        verifyFilterContinues(request, response);
        verifyNoInteractions(jwtService);
        assertNoAuthentication();
    }

    @Test
    void doFilterInternal_ShouldSkipAuthentication_WhenTokenIsInvalid() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithBearerToken(INVALID_TOKEN);
        MockHttpServletResponse response = response();

        when(jwtService.isTokenValid(INVALID_TOKEN))
                .thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verifyFilterContinues(request, response);
        assertNoAuthentication();
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenTokenIsValid() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithBearerToken(VALID_TOKEN);
        MockHttpServletResponse response = response();

        mockValidTokenWithRolesAndPermissions();

        filter.doFilter(request, response, filterChain);

        verifyFilterContinues(request, response);

        Authentication authentication = currentAuthentication();

        assertNotNull(authentication);
        assertEquals(ACCOUNT_ID, authentication.getPrincipal());
        assertHasAuthority(authentication, PLAYER_AUTHORITY);
        assertHasAuthority(authentication, TOURNAMENT_READ_PERMISSION);
    }

    @Test
    void doFilterInternal_ShouldSkipAuthentication_WhenContextAlreadyHasAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithBearerToken(VALID_TOKEN);
        MockHttpServletResponse response = response();

        mockValidTokenWithoutAuthorities();

        filter.doFilter(request, response, filterChain);
        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(2)).doFilter(request, response);
        verify(jwtService, times(2)).isTokenValid(VALID_TOKEN);
        verify(jwtService, times(1)).extractUserId(VALID_TOKEN);
    }

    @Test
    void doFilterInternal_ShouldClearContext_WhenTokenThrowsException() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithBearerToken(BAD_TOKEN);
        MockHttpServletResponse response = response();

        when(jwtService.isTokenValid(BAD_TOKEN))
                .thenThrow(new RuntimeException("Token parse error"));

        filter.doFilter(request, response, filterChain);

        verifyFilterContinues(request, response);
        assertNoAuthentication();
    }

    @Test
    void doFilterInternal_ShouldHandleNullRolesAndPermissions() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithBearerToken(VALID_TOKEN);
        MockHttpServletResponse response = response();

        when(jwtService.isTokenValid(VALID_TOKEN))
                .thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN))
                .thenReturn(ACCOUNT_ID);
        when(jwtService.extractRoles(VALID_TOKEN))
                .thenReturn(null);
        when(jwtService.extractPermissions(VALID_TOKEN))
                .thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verifyFilterContinues(request, response);

        Authentication authentication = currentAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().isEmpty());
    }

    private MockHttpServletRequest requestWithoutAuthorization() {
        return new MockHttpServletRequest();
    }

    private MockHttpServletRequest requestWithAuthorization(String authorizationValue) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION_HEADER, authorizationValue);
        return request;
    }

    private MockHttpServletRequest requestWithBearerToken(String token) {
        return requestWithAuthorization(BEARER_PREFIX + token);
    }

    private MockHttpServletResponse response() {
        return new MockHttpServletResponse();
    }

    private void mockValidTokenWithRolesAndPermissions() {
        when(jwtService.isTokenValid(VALID_TOKEN))
                .thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN))
                .thenReturn(ACCOUNT_ID);
        when(jwtService.extractRoles(VALID_TOKEN))
                .thenReturn(List.of(PLAYER_ROLE));
        when(jwtService.extractPermissions(VALID_TOKEN))
                .thenReturn(List.of(TOURNAMENT_READ_PERMISSION));
    }

    private void mockValidTokenWithoutAuthorities() {
        when(jwtService.isTokenValid(VALID_TOKEN))
                .thenReturn(true);
        when(jwtService.extractUserId(VALID_TOKEN))
                .thenReturn(ACCOUNT_ID);
        when(jwtService.extractRoles(VALID_TOKEN))
                .thenReturn(List.of());
        when(jwtService.extractPermissions(VALID_TOKEN))
                .thenReturn(List.of());
    }

    private void verifyFilterContinues(MockHttpServletRequest request,
                                       MockHttpServletResponse response) throws ServletException, IOException {
        verify(filterChain).doFilter(request, response);
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    private void assertNoAuthentication() {
        assertNull(currentAuthentication());
    }

    private void assertHasAuthority(Authentication authentication, String expectedAuthority) {
        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority -> authority.getAuthority().equals(expectedAuthority))
        );
    }
}