package edu.eci.dosw.unitaria.entity;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.entity.RoleEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountEntityTest {

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        AccountEntity account = new AccountEntity();
        LocalDateTime now = LocalDateTime.now();

        account.setId(1L);
        account.setEmail("juan@escuelaing.edu.co");
        account.setPasswordHash("encoded-password");
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        account.setLastLoginAt(now);

        assertEquals(1L, account.getId());
        assertEquals("juan@escuelaing.edu.co", account.getEmail());
        assertEquals("encoded-password", account.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertEquals(now, account.getCreatedAt());
        assertEquals(now, account.getUpdatedAt());
        assertEquals(now, account.getLastLoginAt());
    }

    @Test
    void defaultConstructor_ShouldInitializeRolesAsEmptyList() {
        AccountEntity account = new AccountEntity();
        assertNotNull(account.getRoles());
        assertTrue(account.getRoles().isEmpty());
    }

    @Test
    void setRoles_ShouldAssignRolesList() {
        AccountEntity account = new AccountEntity();
        RoleEntity role = new RoleEntity();
        role.setName("PLAYER");
        List<RoleEntity> roles = new ArrayList<>();
        roles.add(role);

        account.setRoles(roles);

        assertEquals(1, account.getRoles().size());
        assertEquals("PLAYER", account.getRoles().get(0).getName());
    }

    @Test
    void setRoles_ShouldAssignEmptyList_WhenNullIsPassed() {
        AccountEntity account = new AccountEntity();
        account.setRoles(null);

        assertNotNull(account.getRoles());
        assertTrue(account.getRoles().isEmpty());
    }

    @Test
    void status_ShouldSupportBothValues() {
        AccountEntity account = new AccountEntity();

        account.setStatus(AccountStatus.ACTIVE);
        assertEquals(AccountStatus.ACTIVE, account.getStatus());

        account.setStatus(AccountStatus.INACTIVE);
        assertEquals(AccountStatus.INACTIVE, account.getStatus());
    }

    @Test
    void lastLoginAt_ShouldAllowNull() {
        AccountEntity account = new AccountEntity();
        account.setLastLoginAt(null);
        assertNull(account.getLastLoginAt());
    }
}