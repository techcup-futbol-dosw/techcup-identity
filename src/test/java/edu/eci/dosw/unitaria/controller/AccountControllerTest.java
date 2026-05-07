package edu.eci.dosw.unitaria.controller;

import edu.eci.dosw.controller.AccountController;
import edu.eci.dosw.dto.AccountResponse;
import edu.eci.dosw.dto.RegisterAccountRequest;
import edu.eci.dosw.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @Test
    void register_ShouldReturnCreated_WhenRequestIsValid() {
        RegisterAccountRequest request = new RegisterAccountRequest();
        AccountResponse response = new AccountResponse();
        response.setEmail("juan@escuelaing.edu.co");

        when(accountService.register(request)).thenReturn(response);

        ResponseEntity<AccountResponse> result = accountController.register(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(accountService).register(request);
    }

    @Test
    void register_ShouldThrowException_WhenServiceFails() {
        RegisterAccountRequest request = new RegisterAccountRequest();

        when(accountService.register(request)).thenThrow(new RuntimeException("Email already registered"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountController.register(request));
        assertEquals("Email already registered", ex.getMessage());
        verify(accountService).register(request);
    }

    @Test
    void getById_ShouldReturnOk_WhenAccountExists() {
        AccountResponse response = new AccountResponse();
        response.setEmail("juan@escuelaing.edu.co");

        when(accountService.findById(1L)).thenReturn(response);

        ResponseEntity<AccountResponse> result = accountController.getById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(accountService).findById(1L);
    }

    @Test
    void getById_ShouldThrowException_WhenAccountDoesNotExist() {
        when(accountService.findById(999L)).thenThrow(new RuntimeException("Account not found"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountController.getById(999L));
        assertEquals("Account not found", ex.getMessage());
        verify(accountService).findById(999L);
    }

    @Test
    void deactivate_ShouldReturnNoContent_WhenAccountExists() {
        doNothing().when(accountService).deactivate(1L);

        ResponseEntity<Void> result = accountController.deactivate(1L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(accountService).deactivate(1L);
    }

    @Test
    void deactivate_ShouldThrowException_WhenAccountDoesNotExist() {
        doThrow(new RuntimeException("Account not found")).when(accountService).deactivate(999L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountController.deactivate(999L));
        assertEquals("Account not found", ex.getMessage());
        verify(accountService).deactivate(999L);
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        when(accountService.existsByEmail("juan@escuelaing.edu.co")).thenReturn(true);

        ResponseEntity<Boolean> result = accountController.existsByEmail("juan@escuelaing.edu.co");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody());
        verify(accountService).existsByEmail("juan@escuelaing.edu.co");
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        when(accountService.existsByEmail("noexiste@escuelaing.edu.co")).thenReturn(false);

        ResponseEntity<Boolean> result = accountController.existsByEmail("noexiste@escuelaing.edu.co");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertFalse(result.getBody());
        verify(accountService).existsByEmail("noexiste@escuelaing.edu.co");
    }
}