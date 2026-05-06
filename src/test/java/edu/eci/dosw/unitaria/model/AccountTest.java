package edu.eci.dosw.unitaria.model;

import edu.eci.dosw.entity.AccountStatus;
import edu.eci.dosw.model.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private Account buildAccount() {
        LocalDateTime now = LocalDateTime.now();
        Role role = new Role(1L, "PLAYER", new ArrayList<>());
        return new Account(1L, "juan@escuelaing.edu.co", "encoded-password",
                AccountStatus.ACTIVE, now, now, null, List.of(role));
    }

    @Test
    void constructor_ShouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Role role = new Role(1L, "PLAYER", new ArrayList<>());
        Account account = new Account(1L, "juan@escuelaing.edu.co", "encoded-password",
                AccountStatus.ACTIVE, now, now, now, List.of(role));

        assertEquals(1L, account.getId());
        assertEquals("juan@escuelaing.edu.co", account.getEmail());
        assertEquals("encoded-password", account.getPassword());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertEquals(now, account.getCreatedAt());
        assertEquals(now, account.getUpdatedAt());
        assertEquals(now, account.getLastLoginAt());
        assertEquals(1, account.getRoles().size());
    }

    @Test
    void constructor_ShouldInitializeEmptyRoles_WhenNullIsPassed() {
        LocalDateTime now = LocalDateTime.now();
        Account account = new Account(1L, "juan@escuelaing.edu.co", "encoded-password",
                AccountStatus.ACTIVE, now, now, null, null);

        assertNotNull(account.getRoles());
        assertTrue(account.getRoles().isEmpty());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        Account account = buildAccount();
        LocalDateTime now = LocalDateTime.now();

        account.setId(2L);
        account.setEmail("otro@escuelaing.edu.co");
        account.setPassword("new-password");
        account.setStatus(AccountStatus.INACTIVE);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        account.setLastLoginAt(now);

        assertEquals(2L, account.getId());
        assertEquals("otro@escuelaing.edu.co", account.getEmail());
        assertEquals("new-password", account.getPassword());
        assertEquals(AccountStatus.INACTIVE, account.getStatus());
        assertEquals(now, account.getCreatedAt());
        assertEquals(now, account.getUpdatedAt());
        assertEquals(now, account.getLastLoginAt());
    }

    @Test
    void setRoles_ShouldAssignRoles() {
        Account account = buildAccount();
        Role newRole = new Role(2L, "ADMIN", new ArrayList<>());
        account.setRoles(List.of(newRole));

        assertEquals(1, account.getRoles().size());
        assertEquals("ADMIN", account.getRoles().get(0).getName());
    }

    @Test
    void setRoles_ShouldAssignEmptyList_WhenNullIsPassed() {
        Account account = buildAccount();
        account.setRoles(null);

        assertNotNull(account.getRoles());
        assertTrue(account.getRoles().isEmpty());
    }

    @Test
    void addRole_ShouldAddRole_WhenNotAlreadyPresent() {
        Account account = buildAccount();
        Role newRole = new Role(2L, "ADMIN", new ArrayList<>());

        account.addRole(newRole);

        assertEquals(2, account.getRoles().size());
    }

    @Test
    void addRole_ShouldNotAddDuplicate_WhenRoleAlreadyExists() {
        Account account = buildAccount();
        Role duplicate = new Role(1L, "PLAYER", new ArrayList<>());

        account.addRole(duplicate);

        assertEquals(1, account.getRoles().size());
    }

    @Test
    void addRole_ShouldInitializeList_WhenRolesIsNull() {
        LocalDateTime now = LocalDateTime.now();
        Account account = new Account(1L, "juan@escuelaing.edu.co", "encoded-password",
                AccountStatus.ACTIVE, now, now, null, null);
        Role role = new Role(1L, "PLAYER", new ArrayList<>());

        account.addRole(role);

        assertEquals(1, account.getRoles().size());
    }

    @Test
    void removeRole_ShouldRemoveRole_WhenRoleExists() {
        Account account = buildAccount();
        Role roleToRemove = new Role(1L, "PLAYER", new ArrayList<>());

        account.removeRole(roleToRemove);

        assertTrue(account.getRoles().isEmpty());
    }

    @Test
    void removeRole_ShouldDoNothing_WhenRolesIsNull() {
        LocalDateTime now = LocalDateTime.now();
        Account account = new Account(1L, "juan@escuelaing.edu.co", "encoded-password",
                AccountStatus.ACTIVE, now, now, null, null);
        Role role = new Role(1L, "PLAYER", new ArrayList<>());

        assertDoesNotThrow(() -> account.removeRole(role));
    }

    @Test
    void removeRole_ShouldDoNothing_WhenRoleIsNull() {
        Account account = buildAccount();
        assertDoesNotThrow(() -> account.removeRole(null));
    }

    @Test
    void removeRole_ShouldDoNothing_WhenRoleIdIsNull() {
        Account account = buildAccount();
        Role roleWithNullId = new Role();

        assertDoesNotThrow(() -> account.removeRole(roleWithNullId));
    }

    @Test
    void isActive_ShouldReturnTrue_WhenStatusIsActive() {
        Account account = buildAccount();
        assertTrue(account.isActive());
    }

    @Test
    void isActive_ShouldReturnFalse_WhenStatusIsInactive() {
        Account account = buildAccount();
        account.setStatus(AccountStatus.INACTIVE);
        assertFalse(account.isActive());
    }

    @Test
    void isActive_ShouldReturnFalse_WhenStatusIsNull() {
        Account account = buildAccount();
        account.setStatus(null);
        assertFalse(account.isActive());
    }
}