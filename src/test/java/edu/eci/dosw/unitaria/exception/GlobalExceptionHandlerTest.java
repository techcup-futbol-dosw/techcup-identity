package edu.eci.dosw.unitaria.exception;

import edu.eci.dosw.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/test");
    }

    @Test
    void handleBadRequestExceptions_ShouldReturnBadRequest() {

        InvalidRegistrationDataException exception =
                new InvalidRegistrationDataException("Invalid data");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleBadRequestExceptions(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                "Invalid data",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleConflictExceptions_ShouldReturnConflict() {

        EmailAlreadyRegisteredException exception =
                new EmailAlreadyRegisteredException("Email exists");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleConflictExceptions(exception, request);

        assertEquals(409, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                "Email already registered: Email exists",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleNotFoundExceptions_ShouldReturnNotFound() {

        AccountNotFoundException exception =
                new AccountNotFoundException(1L);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleNotFoundExceptions(exception, request);

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
                "Account not found with id: 1",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleUnauthorizedExceptions_ShouldReturnUnauthorized() {

        InvalidCredentialsException exception =
                new InvalidCredentialsException();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnauthorizedExceptions(exception, request);

        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                "Invalid credentials",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturnConflict() {

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("DB error");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleDataIntegrityViolation(exception, request);

        assertEquals(409, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                "Database integrity violation",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleBusinessException_ShouldReturnBadRequest() {

        BusinessException exception =
                new BusinessException("Business error");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleBusinessException(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                "Business error",
                response.getBody().getMessage()
        );
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {

        Exception exception = new Exception("Unexpected");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleGenericException(exception, request);

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                "An unexpected error occurred",
                response.getBody().getMessage()
        );
    }
    @Test
    void handleValidationException_ShouldReturnBadRequestWithFieldErrorMessage() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError(
                "registerAccountRequest",
                "birthDate",
                "Birth date must be in the past"
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiErrorResponse> response =
                handler.handleValidationException(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                "Birth date must be in the past",
                response.getBody().getMessage()
        );

        assertEquals(
                "/test",
                response.getBody().getPath()
        );

        assertEquals(
                "Bad Request",
                response.getBody().getError()
        );
    }
    @Test
    void handleValidationException_ShouldReturnDefaultMessage_WhenNoFieldErrorsExist() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ApiErrorResponse> response =
                handler.handleValidationException(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                "Invalid request data",
                response.getBody().getMessage()
        );

        assertEquals(
                "/test",
                response.getBody().getPath()
        );

        assertEquals(
                "Bad Request",
                response.getBody().getError()
        );
    }
}