package edu.eci.dosw.unitary.entity;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.entity.PermissionEntity;

import static org.junit.jupiter.api.Assertions.*;

class PermissionEntityTest {

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        PermissionEntity permission = new PermissionEntity();

        permission.setId(1L);
        permission.setName("tournament:read");

        assertEquals(1L, permission.getId());
        assertEquals("tournament:read", permission.getName());
    }

    @Test
    void defaultConstructor_ShouldCreateInstanceWithNullFields() {
        PermissionEntity permission = new PermissionEntity();

        assertNull(permission.getId());
        assertNull(permission.getName());
    }
}