package edu.eci.dosw.unitaria.controller;

import edu.eci.dosw.controller.RoleController;
import edu.eci.dosw.dto.AssignRoleRequest;
import edu.eci.dosw.dto.RemoveRoleRequest;
import edu.eci.dosw.model.Permission;
import edu.eci.dosw.model.Role;
import edu.eci.dosw.service.RoleService;
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
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    @Test
    void assignRole_ShouldReturnNoContent_WhenRequestIsValid() {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setAccountId(1L);
        request.setRoleName("ADMIN");

        doNothing().when(roleService).assignRole(1L, "ADMIN");

        ResponseEntity<Void> result = roleController.assignRole(request);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(roleService).assignRole(1L, "ADMIN");
    }

    @Test
    void assignRole_ShouldThrowException_WhenAccountDoesNotExist() {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setAccountId(999L);
        request.setRoleName("ADMIN");

        doThrow(new RuntimeException("Account not found")).when(roleService).assignRole(999L, "ADMIN");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> roleController.assignRole(request));
        assertEquals("Account not found", ex.getMessage());
        verify(roleService).assignRole(999L, "ADMIN");
    }

    @Test
    void assignRole_ShouldThrowException_WhenRoleDoesNotExist() {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setAccountId(1L);
        request.setRoleName("NONEXISTENT");

        doThrow(new RuntimeException("Role not found")).when(roleService).assignRole(1L, "NONEXISTENT");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> roleController.assignRole(request));
        assertEquals("Role not found", ex.getMessage());
        verify(roleService).assignRole(1L, "NONEXISTENT");
    }

    @Test
    void removeRole_ShouldReturnNoContent_WhenRequestIsValid() {
        RemoveRoleRequest request = new RemoveRoleRequest();
        request.setAccountId(1L);
        request.setRoleName("ADMIN");

        doNothing().when(roleService).removeRole(1L, "ADMIN");

        ResponseEntity<Void> result = roleController.removeRole(request);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(roleService).removeRole(1L, "ADMIN");
    }

    @Test
    void removeRole_ShouldThrowException_WhenAccountDoesNotExist() {
        RemoveRoleRequest request = new RemoveRoleRequest();
        request.setAccountId(999L);
        request.setRoleName("ADMIN");

        doThrow(new RuntimeException("Account not found")).when(roleService).removeRole(999L, "ADMIN");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> roleController.removeRole(request));
        assertEquals("Account not found", ex.getMessage());
        verify(roleService).removeRole(999L, "ADMIN");
    }

    @Test
    void removeRole_ShouldThrowException_WhenRoleDoesNotExist() {
        RemoveRoleRequest request = new RemoveRoleRequest();
        request.setAccountId(1L);
        request.setRoleName("NONEXISTENT");

        doThrow(new RuntimeException("Role not found")).when(roleService).removeRole(1L, "NONEXISTENT");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> roleController.removeRole(request));
        assertEquals("Role not found", ex.getMessage());
        verify(roleService).removeRole(1L, "NONEXISTENT");
    }

    @Test
    void getRolesByAccount_ShouldReturnOk_WhenAccountExists() {
        Role role = new Role();
        role.setName("PLAYER");

        when(roleService.getRolesByAccount(1L)).thenReturn(List.of(role));

        ResponseEntity<List<Role>> result = roleController.getRolesByAccount(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("PLAYER", result.getBody().get(0).getName());
        verify(roleService).getRolesByAccount(1L);
    }

    @Test
    void getRolesByAccount_ShouldReturnEmptyList_WhenAccountHasNoRoles() {
        when(roleService.getRolesByAccount(1L)).thenReturn(List.of());

        ResponseEntity<List<Role>> result = roleController.getRolesByAccount(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(roleService).getRolesByAccount(1L);
    }

    @Test
    void getRolesByAccount_ShouldThrowException_WhenAccountDoesNotExist() {
        when(roleService.getRolesByAccount(999L)).thenThrow(new RuntimeException("Account not found"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> roleController.getRolesByAccount(999L));
        assertEquals("Account not found", ex.getMessage());
        verify(roleService).getRolesByAccount(999L);
    }

    @Test
    void getPermissionsByRole_ShouldReturnOk_WhenRoleExists() {
        Permission permission = new Permission();
        permission.setName("tournament:read");

        when(roleService.getPermissions(1L)).thenReturn(List.of(permission));

        ResponseEntity<List<Permission>> result = roleController.getPermissionsByRole(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("tournament:read", result.getBody().get(0).getName());
        verify(roleService).getPermissions(1L);
    }

    @Test
    void getPermissionsByRole_ShouldReturnEmptyList_WhenRoleHasNoPermissions() {
        when(roleService.getPermissions(1L)).thenReturn(List.of());

        ResponseEntity<List<Permission>> result = roleController.getPermissionsByRole(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(roleService).getPermissions(1L);
    }

    @Test
    void getPermissionsByRole_ShouldThrowException_WhenRoleDoesNotExist() {
        when(roleService.getPermissions(999L)).thenThrow(new RuntimeException("Role not found"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> roleController.getPermissionsByRole(999L));
        assertEquals("Role not found", ex.getMessage());
        verify(roleService).getPermissions(999L);
    }
}