package edu.eci.dosw.unitaria.service;

import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.repository.AccountRepository;
import edu.eci.dosw.repository.RoleRepository;
import edu.eci.dosw.service.RoleService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import edu.eci.dosw.entity.RoleEntity;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static edu.eci.dosw.testutil.TestDataFactory.validAccount;
import static org.mockito.ArgumentMatchers.any;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.Permission;
import edu.eci.dosw.model.Role;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long MISSING_ACCOUNT_ID = 999L;

    private static final Long PLAYER_ROLE_ID = 10L;
    private static final Long ADMIN_ROLE_ID = 20L;
    private static final Long MISSING_ROLE_ID = 999L;

    private static final String EMAIL = "juan@escuelaing.edu.co";

    private static final String PLAYER = "PLAYER";
    private static final String ADMIN = "ADMIN";

    private static final String READ = "READ";
    private static final String WRITE = "WRITE";
    private static final String DELETE = "DELETE";

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private RoleService roleService;

    @Test
    void assignRole_ShouldDoNothing_WhenRoleAlreadyAssigned() {
        AccountEntity accountEntity = accountEntity();
        RoleEntity roleEntity = roleEntity(PLAYER_ROLE_ID, PLAYER);
        Role role = role(PLAYER_ROLE_ID, PLAYER);
        Account account = accountWithRoles(role);

        mockAccountLookup(accountEntity, account);
        mockRoleLookup(PLAYER, roleEntity, role);

        roleService.assignRole(ACCOUNT_ID, PLAYER);

        verify(accountRepository).findById(ACCOUNT_ID);
        verify(roleRepository).findByNameIgnoreCase(PLAYER);
        verify(accountRepository, never()).save(any());
        verify(accountMapper, never()).toEntity(any());

        assertEquals(1, account.getRoles().size());
    }

    @Test
    void assignRole_ShouldThrowException_WhenAccountDoesNotExist() {
        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> roleService.assignRole(ACCOUNT_ID, PLAYER)
        );

        assertEquals("Account not found with id: 1", ex.getMessage());

        verify(accountRepository).findById(ACCOUNT_ID);
        verifyNoInteractions(roleRepository, roleMapper, accountMapper);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void assignRole_ShouldThrowException_WhenRoleDoesNotExist() {
        AccountEntity accountEntity = accountEntity();
        Role existingRole = role(ADMIN_ROLE_ID, ADMIN);
        Account account = accountWithRoles(existingRole);

        mockAccountLookup(accountEntity, account);

        when(roleRepository.findByNameIgnoreCase(ADMIN))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> roleService.assignRole(ACCOUNT_ID, ADMIN)
        );

        assertEquals("Role not found: ADMIN", ex.getMessage());

        verify(accountRepository).findById(ACCOUNT_ID);
        verify(roleRepository).findByNameIgnoreCase(ADMIN);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void removeRole_ShouldRemoveRoleAndSave_WhenRoleIsAssigned() {
        AccountEntity accountEntity = accountEntity();
        RoleEntity roleEntity = roleEntity(PLAYER_ROLE_ID, PLAYER);
        Role role = role(PLAYER_ROLE_ID, PLAYER);
        Account account = accountWithRoles(role);

        mockAccountLookup(accountEntity, account);
        mockRoleLookup(PLAYER, roleEntity, role);

        when(accountMapper.toEntity(any(Account.class)))
                .thenReturn(accountEntity);

        roleService.removeRole(ACCOUNT_ID, PLAYER);

        verify(accountRepository).findById(ACCOUNT_ID);
        verify(roleRepository).findByNameIgnoreCase(PLAYER);
        verify(accountMapper).toEntity(account);
        verify(accountRepository).save(accountEntity);

        assertTrue(account.getRoles().isEmpty());
    }

    @Test
    void removeRole_ShouldDoNothing_WhenRoleIsNotAssigned() {
        AccountEntity accountEntity = accountEntity();
        RoleEntity roleEntity = roleEntity(PLAYER_ROLE_ID, PLAYER);
        Role role = role(PLAYER_ROLE_ID, PLAYER);
        Role anotherRole = role(ADMIN_ROLE_ID, ADMIN);
        Account account = accountWithRoles(anotherRole);

        mockAccountLookup(accountEntity, account);
        mockRoleLookup(PLAYER, roleEntity, role);

        roleService.removeRole(ACCOUNT_ID, PLAYER);

        verify(accountRepository).findById(ACCOUNT_ID);
        verify(roleRepository).findByNameIgnoreCase(PLAYER);
        verify(accountRepository, never()).save(any());
        verify(accountMapper, never()).toEntity(any());

        assertRoleNames(account.getRoles(), ADMIN);
    }

    @Test
    void getRolesByAccount_ShouldReturnRoles_WhenAccountHasRoles() {
        AccountEntity accountEntity = accountEntity();

        Role playerRole = role(PLAYER_ROLE_ID, PLAYER);
        Role adminRole = role(ADMIN_ROLE_ID, ADMIN);
        Account account = accountWithRoles(playerRole, adminRole);

        mockAccountLookup(accountEntity, account);

        List<Role> result = roleService.getRolesByAccount(ACCOUNT_ID);

        assertNotNull(result);
        assertRoleNames(result, PLAYER, ADMIN);

        verify(accountRepository).findById(ACCOUNT_ID);
        verify(accountMapper).toModel(accountEntity);
    }

    @Test
    void getPermissions_ShouldReturnPermissions_WhenRoleHasPermissions() {
        RoleEntity roleEntity = roleEntity(PLAYER_ROLE_ID, PLAYER);
        Role role = roleWithPermissions(
                PLAYER_ROLE_ID,
                permission(READ),
                permission(WRITE)
        );

        mockRoleById(PLAYER_ROLE_ID, roleEntity, role);

        List<Permission> result = roleService.getPermissions(PLAYER_ROLE_ID);

        assertNotNull(result);
        assertPermissionNames(result, READ, WRITE);

        verify(roleRepository).findById(PLAYER_ROLE_ID);
        verify(roleMapper).toModel(roleEntity);
    }

    @Test
    void getPermissions_ShouldReturnEmptyList_WhenRolePermissionsAreNull() {
        RoleEntity roleEntity = roleEntity(PLAYER_ROLE_ID, PLAYER);
        Role role = roleWithPermissions(PLAYER_ROLE_ID);
        role.setPermissions(null);

        mockRoleById(PLAYER_ROLE_ID, roleEntity, role);

        List<Permission> result = roleService.getPermissions(PLAYER_ROLE_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void hasPermission_ShouldReturnTrue_WhenPermissionExists() {
        RoleEntity roleEntity = roleEntity(PLAYER_ROLE_ID, PLAYER);
        Role role = roleWithPermissions(
                PLAYER_ROLE_ID,
                permission(READ),
                permission(WRITE)
        );

        mockRoleById(PLAYER_ROLE_ID, roleEntity, role);

        boolean result = roleService.hasPermission(PLAYER_ROLE_ID, "read");

        assertTrue(result);

        verify(roleRepository).findById(PLAYER_ROLE_ID);
        verify(roleMapper).toModel(roleEntity);
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenPermissionDoesNotExist() {
        RoleEntity roleEntity = roleEntity(PLAYER_ROLE_ID, PLAYER);
        Role role = roleWithPermissions(
                PLAYER_ROLE_ID,
                permission(READ)
        );

        mockRoleById(PLAYER_ROLE_ID, roleEntity, role);

        boolean result = roleService.hasPermission(PLAYER_ROLE_ID, DELETE);

        assertFalse(result);
    }

    @Test
    void getPermissions_ShouldThrowException_WhenRoleDoesNotExist() {
        when(roleRepository.findById(MISSING_ROLE_ID))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> roleService.getPermissions(MISSING_ROLE_ID)
        );

        assertEquals("Role not found: id=999", ex.getMessage());

        verify(roleRepository).findById(MISSING_ROLE_ID);
    }

    @Test
    void getRolesByAccount_ShouldThrowException_WhenAccountDoesNotExist() {
        when(accountRepository.findById(MISSING_ACCOUNT_ID))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> roleService.getRolesByAccount(MISSING_ACCOUNT_ID)
        );

        assertEquals("Account not found with id: 999", ex.getMessage());

        verify(accountRepository).findById(MISSING_ACCOUNT_ID);
    }

    private Account accountWithRoles(Role... roles) {
        return validAccount(EMAIL, roles);
    }

    private AccountEntity accountEntity() {
        AccountEntity entity = new AccountEntity();
        entity.setId(ACCOUNT_ID);
        return entity;
    }

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    private RoleEntity roleEntity(Long id, String name) {
        RoleEntity entity = new RoleEntity();
        entity.setId(id);
        entity.setName(name);
        return entity;
    }

    private Permission permission(String name) {
        Permission permission = new Permission();
        permission.setName(name);
        return permission;
    }

    private Role roleWithPermissions(Long id, Permission... permissions) {
        Role role = role(id, PLAYER);
        role.setPermissions(List.of(permissions));
        return role;
    }

    private void mockAccountLookup(AccountEntity entity, Account account) {
        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(entity));

        when(accountMapper.toModel(entity))
                .thenReturn(account);
    }

    private void mockRoleLookup(String roleName, RoleEntity entity, Role role) {
        when(roleRepository.findByNameIgnoreCase(roleName))
                .thenReturn(Optional.of(entity));

        when(roleMapper.toModel(entity))
                .thenReturn(role);
    }

    private void mockRoleById(Long roleId, RoleEntity entity, Role role) {
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(entity));

        when(roleMapper.toModel(entity))
                .thenReturn(role);
    }

    private void assertRoleNames(List<Role> roles, String... expectedNames) {
        assertEquals(expectedNames.length, roles.size());

        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(expectedNames[i], roles.get(i).getName());
        }
    }

    private void assertPermissionNames(List<Permission> permissions, String... expectedNames) {
        assertEquals(expectedNames.length, permissions.size());

        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(expectedNames[i], permissions.get(i).getName());
        }
    }
}