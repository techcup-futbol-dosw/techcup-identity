package edu.eci.dosw.unitaria.model;

import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.exception.InvalidAccountBuildException;
import edu.eci.dosw.model.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountBuilderTest {

    private AccountBuilder validBuilder() {
        return new AccountBuilder()
                .id(1L)
                .name("Juan")
                .lastName("Roa")
                .birthDate(LocalDate.of(2000, 5, 15))
                .relation(Relation.ESTUDIANTE)
                .semester(7)
                .program("INGENIERIA_SISTEMAS")
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .gender(Gender.MALE)
                .identificationType(IdentificationType.CC)
                .identification("123456789")
                .addRole(new Role(1L, "PLAYER", List.of()));
    }

    @Test
    void build_ShouldCreateAccount_WhenAllFieldsAreValid() {
        Account account = validBuilder().build();

        assertEquals(1L, account.getId());
        assertEquals("Juan", account.getName());
        assertEquals("Roa", account.getLastName());
        assertEquals(LocalDate.of(2000, 5, 15), account.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, account.getRelation());
        assertEquals(7, account.getSemester());
        assertEquals("INGENIERIA_SISTEMAS", account.getProgram());

        assertEquals("juan@escuelaing.edu.co", account.getEmail());
        assertEquals("encoded-password", account.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());

        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());

        assertEquals(Gender.MALE, account.getGender());
        assertEquals(IdentificationType.CC, account.getIdentificationType());
        assertEquals("123456789", account.getIdentification());

        assertEquals(1, account.getRoles().size());
        assertEquals("PLAYER", account.getRoles().get(0).getName());
    }

    @Test
    void build_ShouldSetDefaultStatusActive_WhenStatusIsNull() {
        Account account = validBuilder().status(null).build();

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void build_ShouldUseProvidedStatus_WhenStatusIsSet() {
        Account account = validBuilder()
                .status(AccountStatus.INACTIVE)
                .build();

        assertEquals(AccountStatus.INACTIVE, account.getStatus());
    }

    @Test
    void build_ShouldSetUpdatedAtToCreatedAt_WhenUpdatedAtIsNull() {
        LocalDateTime createdAt = LocalDateTime.now();

        Account account = validBuilder()
                .createdAt(createdAt)
                .updatedAt(null)
                .build();

        assertEquals(createdAt, account.getUpdatedAt());
    }

    @Test
    void build_ShouldSetUpdatedAtToCreatedAt_WhenUpdatedAtIsBeforeCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt.minusDays(1);

        Account account = validBuilder()
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        assertEquals(createdAt, account.getUpdatedAt());
    }

    @Test
    void build_ShouldSetLastLoginAtToNull_WhenItIsBeforeCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime lastLoginAt = createdAt.minusDays(1);

        Account account = validBuilder()
                .createdAt(createdAt)
                .lastLoginAt(lastLoginAt)
                .build();

        assertNull(account.getLastLoginAt());
    }

    @Test
    void build_ShouldKeepLastLoginAt_WhenItIsAfterCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime lastLoginAt = createdAt.plusDays(1);

        Account account = validBuilder()
                .createdAt(createdAt)
                .lastLoginAt(lastLoginAt)
                .build();

        assertEquals(lastLoginAt, account.getLastLoginAt());
    }

    @Test
    void build_ShouldThrowException_WhenNameIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().name(null).build()
        );

        assertEquals("Name is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenNameIsBlank() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().name("   ").build()
        );

        assertEquals("Name is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenLastNameIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().lastName(null).build()
        );

        assertEquals("Last name is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenLastNameIsBlank() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().lastName("   ").build()
        );

        assertEquals("Last name is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenBirthDateIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().birthDate(null).build()
        );

        assertEquals("Birth date is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenRelationIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().relation(null).build()
        );

        assertEquals("Relation is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenSemesterIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().semester(null).build()
        );

        assertEquals("Semester is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenProgramIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().program(null).build()
        );

        assertEquals("Program is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenProgramIsBlank() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().program("   ").build()
        );

        assertEquals("Program is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenEmailIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().email(null).build()
        );

        assertEquals("Email is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenEmailIsBlank() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().email("   ").build()
        );

        assertEquals("Email is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenPasswordHashIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().passwordHash(null).build()
        );

        assertEquals("Password hash is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenPasswordHashIsBlank() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().passwordHash("   ").build()
        );

        assertEquals("Password hash is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenCreatedAtIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().createdAt(null).build()
        );

        assertEquals("CreatedAt is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenGenderIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().gender(null).build()
        );

        assertEquals("Gender is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenIdentificationTypeIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().identificationType(null).build()
        );

        assertEquals("Identification type is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenIdentificationIsNull() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().identification(null).build()
        );

        assertEquals("Identification is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenIdentificationIsBlank() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().identification("   ").build()
        );

        assertEquals("Identification is required", ex.getMessage());
    }

    @Test
    void build_ShouldThrowException_WhenRolesIsEmpty() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().roles(List.of()).build()
        );

        assertEquals("At least one role is required", ex.getMessage());
    }

    @Test
    void addRole_ShouldNotAddNullRole() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().roles(List.of()).addRole(null).build()
        );

        assertEquals("At least one role is required", ex.getMessage());
    }

    @Test
    void roles_ShouldReplaceList_WhenCalledWithValidList() {
        Role role = new Role(1L, "ADMIN", List.of());

        Account account = validBuilder()
                .roles(List.of(role))
                .build();

        assertEquals(1, account.getRoles().size());
        assertEquals("ADMIN", account.getRoles().get(0).getName());
    }

    @Test
    void roles_ShouldAssignEmptyList_WhenNullIsPassed() {
        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                () -> validBuilder().roles(null).build()
        );

        assertEquals("At least one role is required", ex.getMessage());
    }
}