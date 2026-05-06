package edu.eci.dosw.unitaria.controller;

import edu.eci.dosw.controller.AuthController;
import edu.eci.dosw.dto.*;
import edu.eci.dosw.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_ShouldReturnOk_WhenCredentialsAreValid() {
        AuthRequest request = new AuthRequest();
        AuthResponse response = new AuthResponse("access-token", "refresh-token", "Bearer", 3600L);

        when(authService.login(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = authController.login(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(authService).login(request);
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        AuthRequest request = new AuthRequest();

        when(authService.login(request)).thenThrow(new RuntimeException("Invalid credentials"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authController.login(request));
        assertEquals("Invalid credentials", ex.getMessage());
        verify(authService).login(request);
    }

    @Test
    void login_ShouldThrowException_WhenAccountIsInactive() {
        AuthRequest request = new AuthRequest();

        when(authService.login(request)).thenThrow(new RuntimeException("Account not active"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authController.login(request));
        assertEquals("Account not active", ex.getMessage());
        verify(authService).login(request);
    }

    @Test
    void refreshToken_ShouldReturnOk_WhenTokenIsValid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        AuthResponse response = new AuthResponse("new-access-token", "new-refresh-token", "Bearer", 3600L);

        when(authService.refreshToken(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = authController.refreshToken(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(authService).refreshToken(request);
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenIsInvalid() {
        RefreshTokenRequest request = new RefreshTokenRequest();

        when(authService.refreshToken(request)).thenThrow(new RuntimeException("Invalid or expired refresh token"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authController.refreshToken(request));
        assertEquals("Invalid or expired refresh token", ex.getMessage());
        verify(authService).refreshToken(request);
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenIsRevoked() {
        RefreshTokenRequest request = new RefreshTokenRequest();

        when(authService.refreshToken(request)).thenThrow(new RuntimeException("Refresh token revoked"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authController.refreshToken(request));
        assertEquals("Refresh token revoked", ex.getMessage());
        verify(authService).refreshToken(request);
    }

    @Test
    void logout_ShouldReturnNoContent_WhenTokenIsValid() {
        LogoutRequest request = new LogoutRequest();
        doNothing().when(authService).logout(request);

        ResponseEntity<Void> result = authController.logout(request);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(authService).logout(request);
    }

    @Test
    void logout_ShouldThrowException_WhenTokenDoesNotExist() {
        LogoutRequest request = new LogoutRequest();
        doThrow(new RuntimeException("Refresh token not found")).when(authService).logout(request);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authController.logout(request));
        assertEquals("Refresh token not found", ex.getMessage());
        verify(authService).logout(request);
    }

    @Test
    void validateToken_ShouldReturnOk_WhenTokenIsValid() {
        TokenValidationRequest request = new TokenValidationRequest();
        TokenValidationResponse response = new TokenValidationResponse(true, 1L, List.of("PLAYER"), List.of("tournament:read"), "Bearer");

        when(authService.validateToken(request)).thenReturn(response);

        ResponseEntity<TokenValidationResponse> result = authController.validateToken(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(authService).validateToken(request);
    }

    @Test
    void validateToken_ShouldReturnOk_WhenTokenIsInvalid() {
        TokenValidationRequest request = new TokenValidationRequest();
        TokenValidationResponse response = new TokenValidationResponse(false, null, List.of(), List.of(), null);

        when(authService.validateToken(request)).thenReturn(response);

        ResponseEntity<TokenValidationResponse> result = authController.validateToken(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertFalse(result.getBody().isValid());
        verify(authService).validateToken(request);
    }
}