package edu.eci.dosw.unitary.controller;

import edu.eci.dosw.controller.RoleController;
import edu.eci.dosw.dto.AssignRoleRequest;
import edu.eci.dosw.dto.RemoveRoleRequest;
import edu.eci.dosw.dto.RoleSummaryResponse;
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

    private static final Long ACCOUNT_ID = 1L;

    private static final String ACCOUNT_NOT_FOUND = "Account not found";
    private static final String ROLE_NOT_FOUND = "Role not found";
    private static final Long MISSING_ACCOUNT_ID = 999L;
    private static final Long ROLE_ID = 1L;
    private static final String TOURNAMENT_READ = "tournament:read";

    private static final String ADMIN = "ADMIN";
    private static final String PLAYER = "PLAYER";
    private static final String NONEXISTENT = "NONEXISTENT";


    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    @Test
    void assignRole_ShouldReturnNoContent_WhenRequestIsValid() {
        AssignRoleRequest request = assignRoleRequest(ACCOUNT_ID, ADMIN);

        doNothing().when(roleService).assignRole(ACCOUNT_ID, ADMIN);

        ResponseEntity<Void> result = roleController.assignRole(request);

        assertNoContent(result);
        verify(roleService).assignRole(ACCOUNT_ID, ADMIN);
    }

    @Test
    void assignRole_ShouldThrowException_WhenAccountDoesNotExist() {
        AssignRoleRequest request = assignRoleRequest(MISSING_ACCOUNT_ID, ADMIN);

        doThrow(new RuntimeException(ACCOUNT_NOT_FOUND))
                .when(roleService)
                .assignRole(MISSING_ACCOUNT_ID, ADMIN);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> roleController.assignRole(request)
        );

        assertEquals(ACCOUNT_NOT_FOUND, ex.getMessage());
        verify(roleService).assignRole(MISSING_ACCOUNT_ID, ADMIN);
    }

    @Test
    void assignRole_ShouldThrowException_WhenRoleDoesNotExist() {
        AssignRoleRequest request = assignRoleRequest(ACCOUNT_ID, NONEXISTENT);

        doThrow(new RuntimeException(ROLE_NOT_FOUND))
                .when(roleService)
                .assignRole(ACCOUNT_ID, NONEXISTENT);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> roleController.assignRole(request)
        );

        assertEquals(ROLE_NOT_FOUND, ex.getMessage());
        verify(roleService).assignRole(ACCOUNT_ID, NONEXISTENT);
    }

    @Test
    void removeRole_ShouldReturnNoContent_WhenRequestIsValid() {
        RemoveRoleRequest request = removeRoleRequest(ACCOUNT_ID, ADMIN);

        doNothing().when(roleService).removeRole(ACCOUNT_ID, ADMIN);

        ResponseEntity<Void> result = roleController.removeRole(request);

        assertNoContent(result);
        verify(roleService).removeRole(ACCOUNT_ID, ADMIN);
    }

    @Test
    void removeRole_ShouldThrowException_WhenAccountDoesNotExist() {
        RemoveRoleRequest request = removeRoleRequest(MISSING_ACCOUNT_ID, ADMIN);

        doThrow(new RuntimeException(ACCOUNT_NOT_FOUND))
                .when(roleService)
                .removeRole(MISSING_ACCOUNT_ID, ADMIN);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> roleController.removeRole(request)
        );

        assertEquals(ACCOUNT_NOT_FOUND, ex.getMessage());
        verify(roleService).removeRole(MISSING_ACCOUNT_ID, ADMIN);
    }

    @Test
    void removeRole_ShouldThrowException_WhenRoleDoesNotExist() {
        RemoveRoleRequest request = removeRoleRequest(ACCOUNT_ID, NONEXISTENT);

        doThrow(new RuntimeException(ROLE_NOT_FOUND))
                .when(roleService)
                .removeRole(ACCOUNT_ID, NONEXISTENT);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> roleController.removeRole(request)
        );

        assertEquals(ROLE_NOT_FOUND, ex.getMessage());
        verify(roleService).removeRole(ACCOUNT_ID, NONEXISTENT);
    }

    @Test
    void getRolesByAccount_ShouldReturnOk_WhenAccountExists() {
        Role role = role(PLAYER);

        when(roleService.getRolesByAccount(ACCOUNT_ID))
                .thenReturn(List.of(role));

        ResponseEntity<List<Role>> result = roleController.getRolesByAccount(ACCOUNT_ID);

        assertOk(result);
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(PLAYER, result.getBody().get(0).getName());

        verify(roleService).getRolesByAccount(ACCOUNT_ID);
    }

    @Test
    void getRolesByAccount_ShouldReturnEmptyList_WhenAccountHasNoRoles() {
        when(roleService.getRolesByAccount(ACCOUNT_ID))
                .thenReturn(List.of());

        ResponseEntity<List<Role>> result = roleController.getRolesByAccount(ACCOUNT_ID);

        assertOk(result);
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());

        verify(roleService).getRolesByAccount(ACCOUNT_ID);
    }

    @Test
    void getRolesByAccount_ShouldThrowException_WhenAccountDoesNotExist() {
        when(roleService.getRolesByAccount(MISSING_ACCOUNT_ID))
                .thenThrow(new RuntimeException(ACCOUNT_NOT_FOUND));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> roleController.getRolesByAccount(MISSING_ACCOUNT_ID)
        );

        assertEquals(ACCOUNT_NOT_FOUND, ex.getMessage());
        verify(roleService).getRolesByAccount(MISSING_ACCOUNT_ID);
    }

    @Test
    void getPermissionsByRole_ShouldReturnOk_WhenRoleExists() {
        Permission permission = permission(TOURNAMENT_READ);

        when(roleService.getPermissions(ROLE_ID))
                .thenReturn(List.of(permission));

        ResponseEntity<List<Permission>> result = roleController.getPermissionsByRole(ROLE_ID);

        assertOk(result);
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(TOURNAMENT_READ, result.getBody().get(0).getName());

        verify(roleService).getPermissions(ROLE_ID);
    }

    @Test
    void getPermissionsByRole_ShouldReturnEmptyList_WhenRoleHasNoPermissions() {
        when(roleService.getPermissions(ROLE_ID))
                .thenReturn(List.of());

        ResponseEntity<List<Permission>> result = roleController.getPermissionsByRole(ROLE_ID);

        assertOk(result);
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());

        verify(roleService).getPermissions(ROLE_ID);
    }

    @Test
    void getPermissionsByRole_ShouldThrowException_WhenRoleDoesNotExist() {
        when(roleService.getPermissions(MISSING_ACCOUNT_ID))
                .thenThrow(new RuntimeException(ROLE_NOT_FOUND));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> roleController.getPermissionsByRole(MISSING_ACCOUNT_ID)
        );

        assertEquals(ROLE_NOT_FOUND, ex.getMessage());
        verify(roleService).getPermissions(MISSING_ACCOUNT_ID);
    }

    @Test
    void getAllRoles_ShouldReturnOk_WhenRolesExist() {
        RoleSummaryResponse player = new RoleSummaryResponse(1L, "PLAYER");
        RoleSummaryResponse admin = new RoleSummaryResponse(2L, "ADMIN");

        when(roleService.getAllRoles()).thenReturn(List.of(player, admin));

        ResponseEntity<List<RoleSummaryResponse>> result = roleController.getAllRoles();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        assertEquals("PLAYER", result.getBody().get(0).getName());
        assertEquals("ADMIN", result.getBody().get(1).getName());

        verify(roleService).getAllRoles();
    }

    private AssignRoleRequest assignRoleRequest(Long accountId, String roleName) {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setAccountId(accountId);
        request.setRoleName(roleName);
        return request;
    }

    private RemoveRoleRequest removeRoleRequest(Long accountId, String roleName) {
        RemoveRoleRequest request = new RemoveRoleRequest();
        request.setAccountId(accountId);
        request.setRoleName(roleName);
        return request;
    }

    private Role role(String name) {
        Role role = new Role();
        role.setId(ROLE_ID);
        role.setName(name);
        return role;
    }

    private Permission permission(String name) {
        Permission permission = new Permission();
        permission.setName(name);
        return permission;
    }

    private void assertNoContent(ResponseEntity<Void> response) {
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    private void assertOk(ResponseEntity<?> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}