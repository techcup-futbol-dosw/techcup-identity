package edu.eci.dosw.unitary.model;

import edu.eci.dosw.exception.InvalidAccountBuildException;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.model.Gender;
import edu.eci.dosw.model.IdentificationType;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

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
    private void assertBuildFailure(Consumer<AccountBuilder> mutation, String expectedMessage) {
        AccountBuilder builder = validBuilder();
        mutation.accept(builder);

        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                builder::build
        );

        assertEquals(expectedMessage, ex.getMessage());
    }
    private static void clearRolesAndAddNullRole(AccountBuilder builder) {
        builder.roles(List.of());
        builder.addRole(null);
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
        assertBuildFailure(builder -> builder.name(null),"Name is required");
    }

    @Test
    void build_ShouldThrowException_WhenNameIsBlank() {
        assertBuildFailure(builder -> builder.name("  "),"Name is required");

    }

    @Test
    void build_ShouldThrowException_WhenLastNameIsNull() {
        assertBuildFailure(builder -> builder.lastName(null), "Last name is required");
    }


        @Test
    void build_ShouldThrowException_WhenLastNameIsBlank() {
            assertBuildFailure(builder -> builder.lastName("  "), "Last name is required");

        }

    @Test
    void build_ShouldThrowException_WhenBirthDateIsNull() {
        assertBuildFailure(builder -> builder.birthDate(null),"Birth date is required");
    }

    @Test
    void build_ShouldThrowException_WhenRelationIsNull() {
        assertBuildFailure(builder -> builder.relation(null), "Relation is required");
    }

    @Test
    void build_ShouldThrowException_WhenSemesterIsNull() {
        assertBuildFailure(builder -> builder.semester(null), "Semester is required");
    }

    @Test
    void build_ShouldThrowException_WhenProgramIsNull() {
        assertBuildFailure(builder -> builder.program(null), "Program is required");
    }

    @Test
    void build_ShouldThrowException_WhenProgramIsBlank() {
        assertBuildFailure(builder -> builder.program("   "), "Program is required");
    }

    @Test
    void build_ShouldThrowException_WhenEmailIsNull() {
        assertBuildFailure(builder -> builder.email(null), "Email is required");
    }

    @Test
    void build_ShouldThrowException_WhenEmailIsBlank() {
        assertBuildFailure(builder -> builder.email("   "), "Email is required");
    }

    @Test
    void build_ShouldThrowException_WhenPasswordHashIsNull() {
        assertBuildFailure(builder -> builder.passwordHash(null), "Password hash is required");
    }

    @Test
    void build_ShouldThrowException_WhenPasswordHashIsBlank() {
        assertBuildFailure(builder -> builder.passwordHash("   "), "Password hash is required");
    }

    @Test
    void build_ShouldThrowException_WhenCreatedAtIsNull() {
        assertBuildFailure(builder -> builder.createdAt(null), "CreatedAt is required");
    }

    @Test
    void build_ShouldThrowException_WhenGenderIsNull() {
        assertBuildFailure(builder -> builder.gender(null), "Gender is required");
    }

    @Test
    void build_ShouldThrowException_WhenIdentificationTypeIsNull() {
        assertBuildFailure(builder -> builder.identificationType(null), "Identification type is required");
    }

    @Test
    void build_ShouldThrowException_WhenIdentificationIsNull() {
        assertBuildFailure(builder -> builder.identification(null), "Identification is required");
    }

    @Test
    void build_ShouldThrowException_WhenIdentificationIsBlank() {
        assertBuildFailure(builder -> builder.identification("   "), "Identification is required");
    }

    @Test
    void build_ShouldThrowException_WhenRolesIsEmpty() {
        assertBuildFailure(builder -> builder.roles(List.of()), "At least one role is required");
    }

    @Test
    void roles_ShouldAssignEmptyList_WhenNullIsPassed() {
        assertBuildFailure(builder -> builder.roles(null), "At least one role is required");
    }

    @Test
    void addRole_ShouldNotAddNullRole() {
        assertBuildFailure(AccountBuilderTest::clearRolesAndAddNullRole, "At least one role is required");
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
}