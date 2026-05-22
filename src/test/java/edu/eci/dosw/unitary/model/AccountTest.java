package edu.eci.dosw.unitary.model;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.model.Gender;
import edu.eci.dosw.model.IdentificationType;
import edu.eci.dosw.model.Program;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.Role;

class AccountTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long UPDATED_ACCOUNT_ID = 2L;

    private static final Long PLAYER_ROLE_ID = 1L;
    private static final Long ADMIN_ROLE_ID = 2L;

    private static final String PLAYER = "PLAYER";
    private static final String ADMIN = "ADMIN";

    private static final String EMAIL = "juan@escuelaing.edu.co";
    private static final String UPDATED_EMAIL = "otro@escuelaing.edu.co";

    private static final String PASSWORD_HASH = "encoded-password";
    private static final String UPDATED_PASSWORD_HASH = "new-password";

    private static final String NAME = "Juan";
    private static final String UPDATED_NAME = "David";

    private static final String LAST_NAME = "Roa";
    private static final String UPDATED_LAST_NAME = "Hernandez";

    private static final String IDENTIFICATION = "123456789";
    private static final String UPDATED_IDENTIFICATION = "987654321";

    private static final Integer SEMESTER = 7;
    private static final Integer UPDATED_SEMESTER = 8;

    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 5, 15);
    private static final LocalDate UPDATED_BIRTH_DATE = LocalDate.of(1999, 8, 20);

    @Test
    void isActive_ShouldReturnFalse_WhenStatusIsNull() {
        Account account = buildAccount();

        account.setStatus(null);

        assertFalse(account.isActive());
    }

    private Account newAccount(LocalDateTime timestamp,
                               LocalDateTime lastLoginAt,
                               List<Role> roles) {
        return new Account(
                BIRTH_DATE,
                timestamp,
                EMAIL,
                Gender.MALE,
                ACCOUNT_ID,
                IDENTIFICATION,
                IdentificationType.CC,
                lastLoginAt,
                LAST_NAME,
                NAME,
                PASSWORD_HASH,
                Program.SISTEMAS,
                Relation.ESTUDIANTE,
                roles,
                SEMESTER,
                AccountStatus.ACTIVE,
                timestamp
        );
    }

    private Role playerRole() {
        return role(PLAYER_ROLE_ID, PLAYER);
    }

    private Role adminRole() {
        return role(ADMIN_ROLE_ID, ADMIN);
    }

    private Role role(Long id, String name) {
        return new Role(id, name, new ArrayList<>());
    }

    private void assertDefaultAccountFields(Account account) {
        assertEquals(ACCOUNT_ID, account.getId());
        assertEquals(NAME, account.getName());
        assertEquals(LAST_NAME, account.getLastName());
        assertEquals(BIRTH_DATE, account.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, account.getRelation());
        assertEquals(SEMESTER, account.getSemester());
        assertEquals(Program.SISTEMAS, account.getProgram());

        assertEquals(EMAIL, account.getEmail());
        assertEquals(PASSWORD_HASH, account.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());

        assertEquals(Gender.MALE, account.getGender());
        assertEquals(IdentificationType.CC, account.getIdentificationType());
        assertEquals(IDENTIFICATION, account.getIdentification());
    }

    private void assertRole(Role role, Long expectedId, String expectedName) {
        assertEquals(expectedId, role.getId());
        assertEquals(expectedName, role.getName());
    }


    private Account buildAccount() {
        return newAccount(LocalDateTime.now(), null, List.of(playerRole()));
    }

    @Test
    void constructor_ShouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();

        Account account = newAccount(now, now, List.of(playerRole()));

        assertDefaultAccountFields(account);
        assertEquals(now, account.getCreatedAt());
        assertEquals(now, account.getUpdatedAt());
        assertEquals(now, account.getLastLoginAt());

        assertEquals(1, account.getRoles().size());
        assertRole(account.getRoles().get(0), PLAYER_ROLE_ID, PLAYER);
    }

    @Test
    void constructor_ShouldInitializeEmptyRoles_WhenNullIsPassed() {
        Account account = newAccount(LocalDateTime.now(), null, null);

        assertNotNull(account.getRoles());
        assertTrue(account.getRoles().isEmpty());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        Account account = buildAccount();
        LocalDateTime now = LocalDateTime.now();

        account.setId(UPDATED_ACCOUNT_ID);
        account.setName(UPDATED_NAME);
        account.setLastName(UPDATED_LAST_NAME);
        account.setBirthDate(UPDATED_BIRTH_DATE);
        account.setRelation(Relation.ESTUDIANTE);
        account.setSemester(UPDATED_SEMESTER);
        account.setProgram(Program.INDUSTRIAL);

        account.setEmail(UPDATED_EMAIL);
        account.setPasswordHash(UPDATED_PASSWORD_HASH);
        account.setStatus(AccountStatus.INACTIVE);

        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        account.setLastLoginAt(now);

        account.setGender(Gender.MALE);
        account.setIdentificationType(IdentificationType.CC);
        account.setIdentification(UPDATED_IDENTIFICATION);

        assertEquals(UPDATED_ACCOUNT_ID, account.getId());
        assertEquals(UPDATED_NAME, account.getName());
        assertEquals(UPDATED_LAST_NAME, account.getLastName());
        assertEquals(UPDATED_BIRTH_DATE, account.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, account.getRelation());
        assertEquals(UPDATED_SEMESTER, account.getSemester());
        assertEquals(Program.INDUSTRIAL, account.getProgram());

        assertEquals(UPDATED_EMAIL, account.getEmail());
        assertEquals(UPDATED_PASSWORD_HASH, account.getPasswordHash());
        assertEquals(AccountStatus.INACTIVE, account.getStatus());

        assertEquals(now, account.getCreatedAt());
        assertEquals(now, account.getUpdatedAt());
        assertEquals(now, account.getLastLoginAt());

        assertEquals(Gender.MALE, account.getGender());
        assertEquals(IdentificationType.CC, account.getIdentificationType());
        assertEquals(UPDATED_IDENTIFICATION, account.getIdentification());
    }

    @Test
    void setRoles_ShouldAssignRoles() {
        Account account = buildAccount();

        account.setRoles(List.of(adminRole()));

        assertEquals(1, account.getRoles().size());
        assertRole(account.getRoles().get(0), ADMIN_ROLE_ID, ADMIN);
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

        account.addRole(adminRole());

        assertEquals(2, account.getRoles().size());
    }

    @Test
    void addRole_ShouldNotAddDuplicate_WhenRoleAlreadyExists() {
        Account account = buildAccount();

        account.addRole(playerRole());

        assertEquals(1, account.getRoles().size());
    }

    @Test
    void addRole_ShouldInitializeList_WhenRolesIsNull() {
        Account account = newAccount(LocalDateTime.now(), null, null);

        account.addRole(playerRole());

        assertEquals(1, account.getRoles().size());
        assertRole(account.getRoles().get(0), PLAYER_ROLE_ID, PLAYER);
    }

    @Test
    void removeRole_ShouldRemoveRole_WhenRoleExists() {
        Account account = buildAccount();

        account.removeRole(playerRole());

        assertTrue(account.getRoles().isEmpty());
    }

    @Test
    void removeRole_ShouldDoNothing_WhenRolesIsNull() {
        Account account = newAccount(LocalDateTime.now(), null, null);

        assertDoesNotThrow(() -> account.removeRole(playerRole()));
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
}
