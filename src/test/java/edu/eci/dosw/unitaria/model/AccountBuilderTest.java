package edu.eci.dosw.unitaria.model;

import edu.eci.dosw.entity.AccountStatus;
import edu.eci.dosw.model.*;

import edu.eci.dosw.exception.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountBuilderTest {

    private AccountBuilder validBuilder() {
        return new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .addRole(new Role(1L, "PLAYER", List.of()));
    }

    @Test
    void build_ShouldCreateAccount_WhenAllFieldsAreValid() {
        LocalDateTime now = LocalDateTime.now();
        Account account = validBuilder().id(1L).build();

        assertEquals(1L, account.getId());
        assertEquals("juan@escuelaing.edu.co", account.getEmail());
        assertEquals("encoded-password", account.getPassword());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());
        assertEquals(1, account.getRoles().size());
    }

    @Test
    void build_ShouldSetDefaultStatusActive_WhenStatusIsNull() {
        Account account = validBuilder().build();
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void build_ShouldUseProvidedStatus_WhenStatusIsSet() {
        Account account = validBuilder().status(AccountStatus.INACTIVE).build();
        assertEquals(AccountStatus.INACTIVE, account.getStatus());
    }

    @Test
    void build_ShouldSetUpdatedAtToCreatedAt_WhenUpdatedAtIsNull() {
        LocalDateTime createdAt = LocalDateTime.now();
        Account account = validBuilder().createdAt(createdAt).build();
        assertEquals(createdAt, account.getUpdatedAt());
    }

    @Test
    void build_ShouldSetUpdatedAtToCreatedAt_WhenUpdatedAtIsBeforeCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt.minusDays(1);
        Account account = validBuilder().createdAt(createdAt).updatedAt(updatedAt).build();
        assertEquals(createdAt, account.getUpdatedAt());
    }

    @Test
    void build_ShouldSetLastLoginAtToNull_WhenItIsBeforeCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime lastLoginAt = createdAt.minusDays(1);
        Account account = validBuilder().createdAt(createdAt).lastLoginAt(lastLoginAt).build();
        assertNull(account.getLastLoginAt());
    }

    @Test
    void build_ShouldKeepLastLoginAt_WhenItIsAfterCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime lastLoginAt = createdAt.plusDays(1);
        Account account = validBuilder().createdAt(createdAt).lastLoginAt(lastLoginAt).build();
        assertEquals(lastLoginAt, account.getLastLoginAt());
    }

    @Test
    void build_ShouldThrowException_WhenEmailIsNull() {
        AccountBuilder builder = new AccountBuilder()
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .addRole(new Role(1L, "PLAYER", List.of()));

        IllegalStateException ex = assertThrows(edu.eci.dosw.exception.InvalidAccountBuildException, builder::build);
        assertEquals("Email is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenEmailIsBlank() {
        AccountBuilder builder = new AccountBuilder()
                .email("   ")
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .addRole(new Role(1L, "PLAYER", List.of()));

        IllegalStateException ex = assertThrows(edu.eci.dosw.exception.InvalidAccountBuildException, builder::build);
        assertEquals("Email is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenPasswordHashIsNull() {
        AccountBuilder builder = new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .createdAt(LocalDateTime.now())
                .addRole(new Role(1L, "PLAYER", List.of()));

        IllegalStateException ex = assertThrows(edu.eci.dosw.exception.InvalidAccountBuildException, builder::build);
        assertEquals("Password hash is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenPasswordHashIsBlank() {
        AccountBuilder builder = new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .passwordHash("  ")
                .createdAt(LocalDateTime.now())
                .addRole(new Role(1L, "PLAYER", List.of()));

        IllegalStateException ex = assertThrows(edu.eci.dosw.exception.InvalidAccountBuildException, builder::build);
        assertEquals("Password hash is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenCreatedAtIsNull() {
        AccountBuilder builder = new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .addRole(new Role(1L, "PLAYER", List.of()));

        IllegalStateException ex = assertThrows(edu.eci.dosw.exception.InvalidAccountBuildException, builder::build);
        assertEquals("CreatedAt is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenRolesIsEmpty() {
        AccountBuilder builder = new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now());

        IllegalStateException ex = assertThrows(edu.eci.dosw.exception.InvalidAccountBuildException, builder::build);
        assertEquals("At least one role is required", ex.getMessage());
    }

    @Test
    void addRole_ShouldNotAddNullRole() {
        AccountBuilder builder = new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .addRole(null);

        IllegalStateException ex = assertThrows(edu.eci.dosw.exception.InvalidAccountBuildException, builder::build);
        assertEquals("At least one role is required", ex.getMessage());
    }

    @Test
    void roles_ShouldReplaceList_WhenCalledWithValidList() {
        Role role = new Role(1L, "ADMIN", List.of());
        Account account = validBuilder().roles(List.of(role)).build();

        assertEquals(1, account.getRoles().size());
        assertEquals("ADMIN", account.getRoles().get(0).getName());
    }

    @Test
    void roles_ShouldAssignEmptyList_WhenNullIsPassed() {
        AccountBuilder builder = new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .roles(null);

        IllegalStateException ex = assertThrows(edu.eci.dosw.exception.InvalidAccountBuildException, builder::build);
        assertEquals("At least one role is required", ex.getMessage());
    }
}