package edu.eci.dosw.unitary.exception;

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


    private static final int BAD_REQUEST = 400;
    private static final int UNAUTHORIZED = 401;
    private static final int NOT_FOUND = 404;
    private static final int CONFLICT = 409;
    private static final int INTERNAL_SERVER_ERROR = 500;

    private static final String BAD_REQUEST_ERROR = "Bad Request";
    private static final String UNAUTHORIZED_ERROR = "Unauthorized";
    private static final String NOT_FOUND_ERROR = "Not Found";
    private static final String CONFLICT_ERROR = "Conflict";
    private static final String INTERNAL_SERVER_ERROR_NAME = "Internal Server Error";
    private static final String REQUEST_PATH = "/test";

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);

        when(request.getRequestURI()).thenReturn(REQUEST_PATH);
    }


    @Test
    void handleNotFoundExceptions_ShouldReturnNotFound() {
        AccountNotFoundException exception =
                new AccountNotFoundException(1L);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleNotFoundExceptions(exception, request);

        assertErrorResponse(
                response,
                NOT_FOUND,
                NOT_FOUND_ERROR,
                "Account not found with id: 1"
        );
    }

    @Test
    void handleUnauthorizedExceptions_ShouldReturnUnauthorized() {
        InvalidCredentialsException exception =
                new InvalidCredentialsException();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnauthorizedExceptions(exception, request);

        assertErrorResponse(
                response,
                UNAUTHORIZED,
                UNAUTHORIZED_ERROR,
                "Invalid credentials"
        );
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturnConflict() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("DB error");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleDataIntegrityViolation(exception, request);

        assertErrorResponse(
                response,
                CONFLICT,
                CONFLICT_ERROR,
                "Database integrity violation"
        );
    }

    @Test
    void handleBusinessException_ShouldReturnBadRequest() {
        BusinessException exception =
                new BusinessException("Business error");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleBusinessException(exception, request);

        assertErrorResponse(
                response,
                BAD_REQUEST,
                BAD_REQUEST_ERROR,
                "Business error"
        );
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        Exception exception = new Exception("Unexpected");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleGenericException(exception, request);

        assertErrorResponse(
                response,
                INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR_NAME,
                "An unexpected error occurred"
        );
    }

    @Test
    void handleValidationException_ShouldReturnBadRequestWithFieldErrorMessage() {
        MethodArgumentNotValidException exception =
                validationExceptionWithFieldError("birthDate", "Birth date must be in the past");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleValidationException(exception, request);

        assertErrorResponse(
                response,
                BAD_REQUEST,
                BAD_REQUEST_ERROR,
                "Birth date must be in the past"
        );
    }

    @Test
    void handleValidationException_ShouldReturnDefaultMessage_WhenNoFieldErrorsExist() {
        MethodArgumentNotValidException exception =
                validationExceptionWithoutFieldErrors();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleValidationException(exception, request);

        assertErrorResponse(
                response,
                BAD_REQUEST,
                BAD_REQUEST_ERROR,
                "Invalid request data"
        );
    }

    private void assertErrorResponse(ResponseEntity<ApiErrorResponse> response,
                                     int expectedStatus,
                                     String expectedError,
                                     String expectedMessage) {
        assertEquals(expectedStatus, response.getStatusCode().value());
        assertNotNull(response.getBody());

        ApiErrorResponse body = response.getBody();

        assertEquals(expectedStatus, body.getStatus());
        assertEquals(expectedError, body.getError());
        assertEquals(expectedMessage, body.getMessage());
        assertEquals(REQUEST_PATH, body.getPath());
    }

    private MethodArgumentNotValidException validationExceptionWithFieldError(String fieldName,
                                                                              String message) {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError(
                "registerAccountRequest",
                fieldName,
                message
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        return exception;
    }

    private MethodArgumentNotValidException validationExceptionWithoutFieldErrors() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        return exception;
    }


    @Test
    void handleBadRequestExceptions_ShouldReturnBadRequest() {
        InvalidRegistrationDataException exception =
                new InvalidRegistrationDataException("Invalid data");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleBadRequestExceptions(exception, request);

        assertErrorResponse(response, BAD_REQUEST, BAD_REQUEST_ERROR, "Invalid data");
    }

    @Test
    void handleConflictExceptions_ShouldReturnConflict() {
        EmailAlreadyRegisteredException exception =
                new EmailAlreadyRegisteredException("Email exists");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleConflictExceptions(exception, request);

        assertErrorResponse(
                response,
                CONFLICT,
                CONFLICT_ERROR,
                "Email already registered: Email exists"
        );
    }
}