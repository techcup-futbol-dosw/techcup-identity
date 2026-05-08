package edu.eci.dosw.unitaria.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import edu.eci.dosw.exception.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleInvalidRegistrationData_ShouldReturn400() {
        when(request.getRequestURI()).thenReturn("/accounts/register");
        InvalidRegistrationDataException ex = new InvalidRegistrationDataException("Invalid data");

        ResponseEntity<ApiErrorResponse> result = handler.handleInvalidRegistrationData(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid data", result.getBody().getMessage());
        assertEquals("/accounts/register", result.getBody().getPath());
    }

    @Test
    void handleInvalidEmailForRelation_ShouldReturn400() {
        when(request.getRequestURI()).thenReturn("/accounts/register");
        InvalidEmailForRelationException ex = new InvalidEmailForRelationException("Invalid email");

        ResponseEntity<ApiErrorResponse> result = handler.handleInvalidEmailForRelation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid email", result.getBody().getMessage());
    }

    @Test
    void handleMissingRequiredField_ShouldReturn400() {
        when(request.getRequestURI()).thenReturn("/accounts/register");
        MissingRequiredFieldException ex = new MissingRequiredFieldException("email");

        ResponseEntity<ApiErrorResponse> result = handler.handleMissingRequiredField(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("email is required", result.getBody().getMessage());
    }

    @Test
    void handleEmailAlreadyRegistered_ShouldReturn409() {
        when(request.getRequestURI()).thenReturn("/accounts/register");
        EmailAlreadyRegisteredException ex = new EmailAlreadyRegisteredException("juan@escuelaing.edu.co");

        ResponseEntity<ApiErrorResponse> result = handler.handleEmailAlreadyRegistered(ex, request);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("Email already registered: juan@escuelaing.edu.co", result.getBody().getMessage());
    }

    @Test
    void handleAccountNotFound_ShouldReturn404() {
        when(request.getRequestURI()).thenReturn("/accounts/1");
        AccountNotFoundException ex = new AccountNotFoundException(1L);

        ResponseEntity<ApiErrorResponse> result = handler.handleAccountNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void handleRoleNotFound_ShouldReturn404() {
        when(request.getRequestURI()).thenReturn("/roles/1");
        RoleNotFoundException ex = new RoleNotFoundException("PLAYER");

        ResponseEntity<ApiErrorResponse> result = handler.handleRoleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void handleInvalidCredentials_ShouldReturn401() {
        when(request.getRequestURI()).thenReturn("/auth/login");
        InvalidCredentialsException ex = new InvalidCredentialsException();

        ResponseEntity<ApiErrorResponse> result = handler.handleInvalidCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void handleInvalidRefreshToken_ShouldReturn401() {
        when(request.getRequestURI()).thenReturn("/auth/refresh");
        InvalidRefreshTokenException ex = new InvalidRefreshTokenException();

        ResponseEntity<ApiErrorResponse> result = handler.handleInvalidRefreshToken(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void handleRefreshTokenNotFound_ShouldReturn401() {
        when(request.getRequestURI()).thenReturn("/auth/refresh");
        RefreshTokenNotFoundException ex = new RefreshTokenNotFoundException();

        ResponseEntity<ApiErrorResponse> result = handler.handleRefreshTokenNotFound(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void handleRefreshTokenRevoked_ShouldReturn401() {
        when(request.getRequestURI()).thenReturn("/auth/logout");
        RefreshTokenRevokedException ex = new RefreshTokenRevokedException();

        ResponseEntity<ApiErrorResponse> result = handler.handleRefreshTokenRevoked(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void handleAccountNotActive_ShouldReturn401() {
        when(request.getRequestURI()).thenReturn("/auth/login");
        AccountNotActiveException ex = new AccountNotActiveException(1L);

        ResponseEntity<ApiErrorResponse> result = handler.handleAccountNotActive(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void handleBusinessException_ShouldReturn400() {
        when(request.getRequestURI()).thenReturn("/accounts/register");
        BusinessException ex = new BusinessException("Business error");

        ResponseEntity<ApiErrorResponse> result = handler.handleBusinessException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Business error", result.getBody().getMessage());
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        when(request.getRequestURI()).thenReturn("/accounts/register");
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ApiErrorResponse> result = handler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("An unexpected error occurred", result.getBody().getMessage());
    }

    @Test
    void handleInvalidAccountBuild_ShouldReturn400() {
        when(request.getRequestURI()).thenReturn("/accounts/register");
        InvalidAccountBuildException ex = new InvalidAccountBuildException("Email is required");

        ResponseEntity<ApiErrorResponse> result = handler.handleInvalidAccountBuild(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Email is required", result.getBody().getMessage());
    }
}