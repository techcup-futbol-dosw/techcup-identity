package edu.eci.dosw.unitaria.entity;

import edu.eci.dosw.model.Gender;
import edu.eci.dosw.model.IdentificationType;
import edu.eci.dosw.model.Relation;
import org.junit.jupiter.api.Test;

import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.entity.RoleEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountEntityTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long ROLE_ID = 10L;

    private static final String NAME = "Juan";
    private static final String LAST_NAME = "Roa";
    private static final String EMAIL = "juan@escuelaing.edu.co";
    private static final String PASSWORD_HASH = "encoded-password";
    private static final String PROGRAM = "INGENIERIA_SISTEMAS";
    private static final String IDENTIFICATION = "123456789";

    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 5, 15);

    @Test
    void defaultConstructor_ShouldInitializeRolesAsEmptyList() {
        AccountEntity entity = new AccountEntity();

        assertNotNull(entity.getRoles());
        assertTrue(entity.getRoles().isEmpty());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt.plusMinutes(5);
        LocalDateTime lastLoginAt = createdAt.plusHours(1);

        RoleEntity role = role(ROLE_ID, "PLAYER");

        AccountEntity entity = new AccountEntity();
        entity.setId(ACCOUNT_ID);
        entity.setName(NAME);
        entity.setLastName(LAST_NAME);
        entity.setBirthDate(BIRTH_DATE);
        entity.setRelation(Relation.ESTUDIANTE);
        entity.setSemester(7);
        entity.setProgram(PROGRAM);
        entity.setEmail(EMAIL);
        entity.setPasswordHash(PASSWORD_HASH);
        entity.setStatus(AccountStatus.ACTIVE);
        entity.setGender(Gender.MALE);
        entity.setIdentificationType(IdentificationType.CC);
        entity.setIdentification(IDENTIFICATION);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        entity.setLastLoginAt(lastLoginAt);
        entity.setRoles(List.of(role));

        assertEquals(ACCOUNT_ID, entity.getId());
        assertEquals(NAME, entity.getName());
        assertEquals(LAST_NAME, entity.getLastName());
        assertEquals(BIRTH_DATE, entity.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, entity.getRelation());
        assertEquals(7, entity.getSemester());
        assertEquals(PROGRAM, entity.getProgram());

        assertEquals(EMAIL, entity.getEmail());
        assertEquals(PASSWORD_HASH, entity.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, entity.getStatus());

        assertEquals(Gender.MALE, entity.getGender());
        assertEquals(IdentificationType.CC, entity.getIdentificationType());
        assertEquals(IDENTIFICATION, entity.getIdentification());

        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
        assertEquals(lastLoginAt, entity.getLastLoginAt());

        assertNotNull(entity.getRoles());
        assertEquals(1, entity.getRoles().size());
        assertEquals("PLAYER", entity.getRoles().get(0).getName());
    }

    @Test
    void setRoles_ShouldAssignRoles_WhenListIsNotNull() {
        AccountEntity entity = new AccountEntity();

        RoleEntity playerRole = role(ROLE_ID, "PLAYER");
        RoleEntity adminRole = role(20L, "ADMIN");

        entity.setRoles(List.of(playerRole, adminRole));

        assertNotNull(entity.getRoles());
        assertEquals(2, entity.getRoles().size());
        assertEquals("PLAYER", entity.getRoles().get(0).getName());
        assertEquals("ADMIN", entity.getRoles().get(1).getName());
    }

    @Test
    void setRoles_ShouldAssignEmptyList_WhenNullIsPassed() {
        AccountEntity entity = new AccountEntity();
        entity.setRoles(null);

        assertNotNull(entity.getRoles());
        assertTrue(entity.getRoles().isEmpty());
    }

    @Test
    void lastLoginAt_ShouldAllowNullValue() {
        AccountEntity entity = new AccountEntity();
        entity.setLastLoginAt(null);

        assertNull(entity.getLastLoginAt());
    }

    @Test
    void shouldAllowChangingStatus() {
        AccountEntity entity = new AccountEntity();

        entity.setStatus(AccountStatus.ACTIVE);
        assertEquals(AccountStatus.ACTIVE, entity.getStatus());

        entity.setStatus(AccountStatus.INACTIVE);
        assertEquals(AccountStatus.INACTIVE, entity.getStatus());
    }

    @Test
    void shouldAllowChangingEnums() {
        AccountEntity entity = new AccountEntity();

        entity.setRelation(Relation.ESTUDIANTE);
        entity.setGender(Gender.MALE);
        entity.setIdentificationType(IdentificationType.CC);

        assertEquals(Relation.ESTUDIANTE, entity.getRelation());
        assertEquals(Gender.MALE, entity.getGender());
        assertEquals(IdentificationType.CC, entity.getIdentificationType());
    }

    private RoleEntity role(Long id, String name) {
        RoleEntity role = new RoleEntity();
        role.setId(id);
        role.setName(name);
        role.setPermissions(new ArrayList<>());
        return role;
    }
}