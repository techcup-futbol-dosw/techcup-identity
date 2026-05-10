package edu.eci.dosw.unitaria.model;

import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.model.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private Account buildAccount() {
        LocalDateTime now = LocalDateTime.now();
        Role role = new Role(1L, "PLAYER", new ArrayList<>());

        return new Account(
                LocalDate.of(2000, 5, 15),
                now,
                "juan@escuelaing.edu.co",
                Gender.MALE,
                1L,
                "123456789",
                IdentificationType.CC,
                null,
                "Roa",
                "Juan",
                "encoded-password",
                "INGENIERIA_SISTEMAS",
                Relation.ESTUDIANTE,
                List.of(role),
                7,
                AccountStatus.ACTIVE,
                now
        );
    }

    @Test
    void constructor_ShouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate birthDate = LocalDate.of(2000, 5, 15);
        Role role = new Role(1L, "PLAYER", new ArrayList<>());

        Account account = new Account(
                birthDate,
                now,
                "juan@escuelaing.edu.co",
                Gender.MALE,
                1L,
                "123456789",
                IdentificationType.CC,
                now,
                "Roa",
                "Juan",
                "encoded-password",
                "INGENIERIA_SISTEMAS",
                Relation.ESTUDIANTE,
                List.of(role),
                7,
                AccountStatus.ACTIVE,
                now
        );

        assertEquals(1L, account.getId());
        assertEquals("Juan", account.getName());
        assertEquals("Roa", account.getLastName());
        assertEquals(birthDate, account.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, account.getRelation());
        assertEquals(7, account.getSemester());
        assertEquals("INGENIERIA_SISTEMAS", account.getProgram());

        assertEquals("juan@escuelaing.edu.co", account.getEmail());
        assertEquals("encoded-password", account.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());

        assertEquals(now, account.getCreatedAt());
        assertEquals(now, account.getUpdatedAt());
        assertEquals(now, account.getLastLoginAt());

        assertEquals(Gender.MALE, account.getGender());
        assertEquals(IdentificationType.CC, account.getIdentificationType());
        assertEquals("123456789", account.getIdentification());

        assertEquals(1, account.getRoles().size());
        assertEquals("PLAYER", account.getRoles().get(0).getName());
    }

    @Test
    void constructor_ShouldInitializeEmptyRoles_WhenNullIsPassed() {
        LocalDateTime now = LocalDateTime.now();

        Account account = new Account(
                LocalDate.of(2000, 5, 15),
                now,
                "juan@escuelaing.edu.co",
                Gender.MALE,
                1L,
                "123456789",
                IdentificationType.CC,
                null,
                "Roa",
                "Juan",
                "encoded-password",
                "INGENIERIA_SISTEMAS",
                Relation.ESTUDIANTE,
                null,
                7,
                AccountStatus.ACTIVE,
                now
        );

        assertNotNull(account.getRoles());
        assertTrue(account.getRoles().isEmpty());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        Account account = buildAccount();
        LocalDateTime now = LocalDateTime.now();
        LocalDate birthDate = LocalDate.of(1999, 8, 20);

        account.setId(2L);
        account.setName("David");
        account.setLastName("Hernandez");
        account.setBirthDate(birthDate);
        account.setRelation(Relation.ESTUDIANTE);
        account.setSemester(8);
        account.setProgram("INGENIERIA_INDUSTRIAL");

        account.setEmail("otro@escuelaing.edu.co");
        account.setPasswordHash("new-password");
        account.setStatus(AccountStatus.INACTIVE);

        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        account.setLastLoginAt(now);

        account.setGender(Gender.MALE);
        account.setIdentificationType(IdentificationType.CC);
        account.setIdentification("987654321");

        assertEquals(2L, account.getId());
        assertEquals("David", account.getName());
        assertEquals("Hernandez", account.getLastName());
        assertEquals(birthDate, account.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, account.getRelation());
        assertEquals(8, account.getSemester());
        assertEquals("INGENIERIA_INDUSTRIAL", account.getProgram());

        assertEquals("otro@escuelaing.edu.co", account.getEmail());
        assertEquals("new-password", account.getPasswordHash());
        assertEquals(AccountStatus.INACTIVE, account.getStatus());

        assertEquals(now, account.getCreatedAt());
        assertEquals(now, account.getUpdatedAt());
        assertEquals(now, account.getLastLoginAt());

        assertEquals(Gender.MALE, account.getGender());
        assertEquals(IdentificationType.CC, account.getIdentificationType());
        assertEquals("987654321", account.getIdentification());
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

        Account account = new Account(
                LocalDate.of(2000, 5, 15),
                now,
                "juan@escuelaing.edu.co",
                Gender.MALE,
                1L,
                "123456789",
                IdentificationType.CC,
                null,
                "Roa",
                "Juan",
                "encoded-password",
                "INGENIERIA_SISTEMAS",
                Relation.ESTUDIANTE,
                null,
                7,
                AccountStatus.ACTIVE,
                now
        );

        Role role = new Role(1L, "PLAYER", new ArrayList<>());

        account.addRole(role);

        assertEquals(1, account.getRoles().size());
        assertEquals("PLAYER", account.getRoles().get(0).getName());
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

        Account account = new Account(
                LocalDate.of(2000, 5, 15),
                now,
                "juan@escuelaing.edu.co",
                Gender.MALE,
                1L,
                "123456789",
                IdentificationType.CC,
                null,
                "Roa",
                "Juan",
                "encoded-password",
                "INGENIERIA_SISTEMAS",
                Relation.ESTUDIANTE,
                null,
                7,
                AccountStatus.ACTIVE,
                now
        );

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