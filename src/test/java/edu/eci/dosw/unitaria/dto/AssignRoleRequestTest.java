package edu.eci.dosw.unitaria.dto;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.dto.AssignRoleRequest;

import static org.junit.jupiter.api.Assertions.*;

class AssignRoleRequestTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithNullFields() {
        AssignRoleRequest request = new AssignRoleRequest();
        assertNull(request.getAccountId());
        assertNull(request.getRoleName());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setAccountId(1L);
        request.setRoleName("ADMIN");

        assertEquals(1L, request.getAccountId());
        assertEquals("ADMIN", request.getRoleName());
    }
}