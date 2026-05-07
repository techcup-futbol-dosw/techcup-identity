package edu.eci.dosw.unitaria.dto;

import edu.eci.dosw.dto.AccountResponse;
import edu.eci.dosw.entity.AccountStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountResponseTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithNullFields() {
        AccountResponse response = new AccountResponse();
        assertNull(response.getId());
        assertNull(response.getEmail());
        assertNull(response.getStatus());
        assertNull(response.getCreatedAt());
        assertNull(response.getRoles());
    }

    @Test
    void parameterizedConstructor_ShouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        AccountResponse response = new AccountResponse(1L, "juan@escuelaing.edu.co", AccountStatus.ACTIVE, now, List.of("PLAYER"));

        assertEquals(1L, response.getId());
        assertEquals("juan@escuelaing.edu.co", response.getEmail());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(now, response.getCreatedAt());
        assertEquals(List.of("PLAYER"), response.getRoles());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        AccountResponse response = new AccountResponse();
        response.setId(1L);
        response.setEmail("juan@escuelaing.edu.co");
        response.setStatus(AccountStatus.INACTIVE);
        response.setCreatedAt(now);
        response.setRoles(List.of("ADMIN"));

        assertEquals(1L, response.getId());
        assertEquals("juan@escuelaing.edu.co", response.getEmail());
        assertEquals(AccountStatus.INACTIVE, response.getStatus());
        assertEquals(now, response.getCreatedAt());
        assertEquals(List.of("ADMIN"), response.getRoles());
    }
}