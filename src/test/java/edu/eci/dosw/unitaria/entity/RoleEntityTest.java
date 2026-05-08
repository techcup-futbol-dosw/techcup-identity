package edu.eci.dosw.unitaria.entity;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.entity.RoleEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleEntityTest {

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        RoleEntity role = new RoleEntity();

        role.setId(1L);
        role.setName("PLAYER");

        assertEquals(1L, role.getId());
        assertEquals("PLAYER", role.getName());
    }

    @Test
    void defaultConstructor_ShouldInitializePermissionsAsEmptyList() {
        RoleEntity role = new RoleEntity();

        assertNotNull(role.getPermissions());
        assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    void setPermissions_ShouldAssignPermissionsList() {
        RoleEntity role = new RoleEntity();
        PermissionEntity permission = new PermissionEntity();
        permission.setName("tournament:read");
        List<PermissionEntity> permissions = new ArrayList<>();
        permissions.add(permission);

        role.setPermissions(permissions);

        assertEquals(1, role.getPermissions().size());
        assertEquals("tournament:read", role.getPermissions().get(0).getName());
    }

    @Test
    void setPermissions_ShouldAssignEmptyList_WhenNullIsPassed() {
        RoleEntity role = new RoleEntity();
        role.setPermissions(null);

        assertNotNull(role.getPermissions());
        assertTrue(role.getPermissions().isEmpty());
    }
}