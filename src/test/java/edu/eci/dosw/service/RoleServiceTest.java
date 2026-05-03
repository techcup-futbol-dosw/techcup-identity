package edu.eci.dosw.service;

import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.model.Permission;
import edu.eci.dosw.model.Role;
import edu.eci.dosw.repository.AccountRepository;
import edu.eci.dosw.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import edu.eci.dosw.entity.RoleEntity;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

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
        // Arrange
        Long accountId = 1L;
        String roleName = "PLAYER";

        AccountEntity accountEntity = new AccountEntity();
        RoleEntity roleEntity = new RoleEntity();

        Role role = new Role();
        role.setId(10L);
        role.setName("PLAYER");

        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .id(1L)
                .createdAt(LocalDateTime.now())
                .roles(new ArrayList<>(List.of(role)));
        Account account = accountBuilder.build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(accountEntity));

        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);

        when(roleRepository.findByNameIgnoreCase(roleName))
                .thenReturn(Optional.of(roleEntity));

        when(roleMapper.toModel(roleEntity))
                .thenReturn(role);

        // Act
        roleService.assignRole(accountId, roleName);

        // Assert
        verify(accountRepository).findById(accountId);
        verify(roleRepository).findByNameIgnoreCase(roleName);
        verify(accountRepository, never()).save(any());
        verify(accountMapper, never()).toEntity(any());

        assertEquals(1, account.getRoles().size());
    }

    @Test
    void assignRole_ShouldThrowException_WhenAccountDoesNotExist() {
        // Arrange
        Long accountId = 1L;

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roleService.assignRole(accountId, "PLAYER"));

        // Assert
        assertEquals("Account not found", ex.getMessage());

        verify(accountRepository).findById(accountId);
        verifyNoInteractions(roleRepository, roleMapper);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void assignRole_ShouldThrowException_WhenRoleDoesNotExist() {
        // Arrange
        Long accountId = 1L;
        String roleName = "PLAYER";

        AccountEntity accountEntity = new AccountEntity();
        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .id(1L)
                .createdAt(LocalDateTime.now())
                .addRole(new Role());
        Account account = accountBuilder.build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(accountEntity));

        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);

        when(roleRepository.findByNameIgnoreCase(roleName))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roleService.assignRole(accountId, roleName));

        // Assert
        assertEquals("Role not found", ex.getMessage());

        verify(accountRepository).findById(accountId);
        verify(roleRepository).findByNameIgnoreCase(roleName);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void removeRole_ShouldRemoveRoleAndSave_WhenRoleIsAssigned() {
        // Arrange
        Long accountId = 1L;
        String roleName = "PLAYER";

        AccountEntity accountEntity = new AccountEntity();
        RoleEntity roleEntity = new RoleEntity();

        Role role = new Role();
        role.setId(10L);
        role.setName("PLAYER");
        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .id(1L)
                .createdAt(LocalDateTime.now())
                .addRole(role);
        Account account = accountBuilder.build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(accountEntity));

        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);

        when(roleRepository.findByNameIgnoreCase(roleName))
                .thenReturn(Optional.of(roleEntity));

        when(roleMapper.toModel(roleEntity))
                .thenReturn(role);

        when(accountMapper.toEntity(any(Account.class)))
                .thenReturn(accountEntity);

        // Act
        roleService.removeRole(accountId, roleName);

        // Assert
        verify(accountRepository).findById(accountId);
        verify(roleRepository).findByNameIgnoreCase(roleName);
        verify(accountMapper).toEntity(account);
        verify(accountRepository).save(accountEntity);

        assertTrue(account.getRoles().isEmpty());
    }

    @Test
    void removeRole_ShouldDoNothing_WhenRoleIsNotAssigned() {
        // Arrange
        Long accountId = 1L;
        String roleName = "PLAYER";

        AccountEntity accountEntity = new AccountEntity();
        RoleEntity roleEntity = new RoleEntity();

        Role role = new Role();
        role.setId(10L);
        role.setName("PLAYER");

        Role anotherRole = new Role();
        anotherRole.setId(20L);
        anotherRole.setName("ADMIN");

        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .id(1L)
                .createdAt(LocalDateTime.now())
                .addRole(anotherRole);
        Account account = accountBuilder.build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(accountEntity));

        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);

        when(roleRepository.findByNameIgnoreCase(roleName))
                .thenReturn(Optional.of(roleEntity));

        when(roleMapper.toModel(roleEntity))
                .thenReturn(role);

        // Act
        roleService.removeRole(accountId, roleName);

        // Assert
        verify(accountRepository).findById(accountId);
        verify(roleRepository).findByNameIgnoreCase(roleName);
        verify(accountRepository, never()).save(any());
        verify(accountMapper, never()).toEntity(any());

        assertEquals(1, account.getRoles().size());
        assertEquals("ADMIN", account.getRoles().get(0).getName());
    }

    @Test
    void getRolesByAccount_ShouldReturnRoles_WhenAccountHasRoles() {
        // Arrange
        Long accountId = 1L;

        AccountEntity accountEntity = new AccountEntity();

        Role role1 = new Role();
        role1.setId(10L);
        role1.setName("PLAYER");

        Role role2 = new Role();
        role2.setId(20L);
        role2.setName("ADMIN");

        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .id(1L)
                .createdAt(LocalDateTime.now())
                .addRole(role1)
                .addRole(role2);
        Account account = accountBuilder.build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(accountEntity));

        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);

        // Act
        List<Role> result = roleService.getRolesByAccount(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PLAYER", result.get(0).getName());
        assertEquals("ADMIN", result.get(1).getName());

        verify(accountRepository).findById(accountId);
        verify(accountMapper).toModel(accountEntity);
    }


    @Test
    void getPermissions_ShouldReturnPermissions_WhenRoleHasPermissions() {
        // Arrange
        Long roleId = 10L;

        RoleEntity roleEntity = new RoleEntity();

        Permission p1 = new Permission();
        p1.setName("READ");

        Permission p2 = new Permission();
        p2.setName("WRITE");

        Role role = new Role();
        role.setId(roleId);
        role.setPermissions(List.of(p1, p2));

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(roleEntity));

        when(roleMapper.toModel(roleEntity))
                .thenReturn(role);

        // Act
        List<Permission> result = roleService.getPermissions(roleId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("READ", result.get(0).getName());
        assertEquals("WRITE", result.get(1).getName());

        verify(roleRepository).findById(roleId);
        verify(roleMapper).toModel(roleEntity);
    }

    @Test
    void getPermissions_ShouldReturnEmptyList_WhenRolePermissionsAreNull() {
        // Arrange
        Long roleId = 10L;

        RoleEntity roleEntity = new RoleEntity();

        Role role = new Role();
        role.setId(roleId);
        role.setPermissions(null);

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(roleEntity));

        when(roleMapper.toModel(roleEntity))
                .thenReturn(role);

        // Act
        List<Permission> result = roleService.getPermissions(roleId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void hasPermission_ShouldReturnTrue_WhenPermissionExists() {
        // Arrange
        Long roleId = 10L;

        RoleEntity roleEntity = new RoleEntity();

        Permission read = new Permission();
        read.setName("READ");

        Permission write = new Permission();
        write.setName("WRITE");

        Role role = new Role();
        role.setId(roleId);
        role.setPermissions(List.of(read, write));

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(roleEntity));

        when(roleMapper.toModel(roleEntity))
                .thenReturn(role);

        // Act
        boolean result = roleService.hasPermission(roleId, "read");

        // Assert
        assertTrue(result);
        verify(roleRepository).findById(roleId);
        verify(roleMapper).toModel(roleEntity);
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenPermissionDoesNotExist() {
        // Arrange
        Long roleId = 10L;

        RoleEntity roleEntity = new RoleEntity();

        Permission read = new Permission();
        read.setName("READ");

        Role role = new Role();
        role.setId(roleId);
        role.setPermissions(List.of(read));

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(roleEntity));

        when(roleMapper.toModel(roleEntity))
                .thenReturn(role);

        // Act
        boolean result = roleService.hasPermission(roleId, "DELETE");

        // Assert
        assertFalse(result);
    }

    @Test
    void getPermissions_ShouldThrowException_WhenRoleDoesNotExist() {
        // Arrange
        Long roleId = 999L;

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roleService.getPermissions(roleId));

        // Assert
        assertEquals("Role not found", ex.getMessage());
        verify(roleRepository).findById(roleId);
    }

    @Test
    void getRolesByAccount_ShouldThrowException_WhenAccountDoesNotExist() {
        // Arrange
        Long accountId = 999L;

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roleService.getRolesByAccount(accountId));

        // Assert
        assertEquals("Account not found", ex.getMessage());
        verify(accountRepository).findById(accountId);
    }
}