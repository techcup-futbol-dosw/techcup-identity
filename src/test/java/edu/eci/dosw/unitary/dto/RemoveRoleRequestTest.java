package edu.eci.dosw.unitary.dto;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.dto.RemoveRoleRequest;

import static org.junit.jupiter.api.Assertions.*;

class RemoveRoleRequestTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithNullFields() {
        RemoveRoleRequest request = new RemoveRoleRequest();
        assertNull(request.getAccountId());
        assertNull(request.getRoleName());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        RemoveRoleRequest request = new RemoveRoleRequest();
        request.setAccountId(1L);
        request.setRoleName("ADMIN");

        assertEquals(1L, request.getAccountId());
        assertEquals("ADMIN", request.getRoleName());
    }
}