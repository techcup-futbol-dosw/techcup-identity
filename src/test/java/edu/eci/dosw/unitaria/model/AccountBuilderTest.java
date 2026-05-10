package edu.eci.dosw.unitaria.model;

import edu.eci.dosw.exception.InvalidAccountBuildException;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.model.Gender;
import edu.eci.dosw.model.IdentificationType;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AccountBuilderTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long ROLE_ID = 1L;

    private static final String NAME = "Juan";
    private static final String LAST_NAME = "Roa";
    private static final String EMAIL = "juan@escuelaing.edu.co";
    private static final String PASSWORD_HASH = "encoded-password";
    private static final String PROGRAM = "INGENIERIA_SISTEMAS";
    private static final String IDENTIFICATION = "123456789";
    private static final String PLAYER = "PLAYER";
    private static final String ADMIN = "ADMIN";

    private static final Integer SEMESTER = 7;
    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 5, 15);

    private AccountBuilder validBuilder() {
        return new AccountBuilder()
                .id(ACCOUNT_ID)
                .name(NAME)
                .lastName(LAST_NAME)
                .birthDate(BIRTH_DATE)
                .relation(Relation.ESTUDIANTE)
                .semester(SEMESTER)
                .program(PROGRAM)
                .email(EMAIL)
                .passwordHash(PASSWORD_HASH)
                .createdAt(LocalDateTime.now())
                .gender(Gender.MALE)
                .identificationType(IdentificationType.CC)
                .identification(IDENTIFICATION)
                .addRole(role(ROLE_ID, PLAYER));
    }

    private Role role(Long id, String name) {
        return new Role(id, name, List.of());
    }

    private void assertDefaultAccountFields(Account account) {
        assertEquals(ACCOUNT_ID, account.getId());
        assertEquals(NAME, account.getName());
        assertEquals(LAST_NAME, account.getLastName());
        assertEquals(BIRTH_DATE, account.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, account.getRelation());
        assertEquals(SEMESTER, account.getSemester());
        assertEquals(PROGRAM, account.getProgram());

        assertEquals(EMAIL, account.getEmail());
        assertEquals(PASSWORD_HASH, account.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());

        assertEquals(Gender.MALE, account.getGender());
        assertEquals(IdentificationType.CC, account.getIdentificationType());
        assertEquals(IDENTIFICATION, account.getIdentification());

        assertEquals(1, account.getRoles().size());
    }

    private void assertRole(Role role, String expectedName) {
        assertEquals(expectedName, role.getName());
    }
    private static Stream<Arguments> invalidRequiredFields() {
        return Stream.of(
                arguments((Consumer<AccountBuilder>) builder -> builder.name(null), "Name is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.name("   "), "Name is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.lastName(null), "Last name is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.lastName("   "), "Last name is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.birthDate(null), "Birth date is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.relation(null), "Relation is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.semester(null), "Semester is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.program(null), "Program is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.program("   "), "Program is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.email(null), "Email is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.email("   "), "Email is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.passwordHash(null), "Password hash is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.passwordHash("   "), "Password hash is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.createdAt(null), "CreatedAt is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.gender(null), "Gender is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.identificationType(null), "Identification type is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.identification(null), "Identification is required"),
                arguments((Consumer<AccountBuilder>) builder -> builder.identification("   "), "Identification is required")
        );
    }

    private static Stream<Arguments> invalidRoles() {
        return Stream.of(
                arguments(
                        (Consumer<AccountBuilder>) builder -> builder.roles(List.of()),
                        "At least one role is required"
                ),
                arguments(
                        (Consumer<AccountBuilder>) builder -> builder.roles(null),
                        "At least one role is required"
                ),
                arguments(
                        (Consumer<AccountBuilder>) builder -> builder.roles(List.of()).addRole(null),
                        "At least one role is required"
                )
        );
    }


    @Test
    void build_ShouldCreateAccount_WhenAllFieldsAreValid() {
        Account account = validBuilder().build();

        assertDefaultAccountFields(account);
        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());
        assertRole(account.getRoles().get(0), PLAYER);
    }

    @Test
    void build_ShouldSetDefaultStatusActive_WhenStatusIsNull() {
        Account account = validBuilder()
                .status(null)
                .build();

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

    @ParameterizedTest(name = "{1}")
    @MethodSource("invalidRequiredFields")
    void build_ShouldThrowException_WhenRequiredFieldIsInvalid(
            Consumer<AccountBuilder> invalidMutation,
            String expectedMessage
    ) {
        AccountBuilder builder = validBuilder();
        invalidMutation.accept(builder);

        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                builder::build
        );

        assertEquals(expectedMessage, ex.getMessage());
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("invalidRoles")
    void build_ShouldThrowException_WhenRolesAreInvalid(
            Consumer<AccountBuilder> invalidMutation,
            String expectedMessage
    ) {
        AccountBuilder builder = validBuilder();
        invalidMutation.accept(builder);

        InvalidAccountBuildException ex = assertThrows(
                InvalidAccountBuildException.class,
                builder::build
        );

        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    void roles_ShouldReplaceList_WhenCalledWithValidList() {
        Role role = role(ROLE_ID, ADMIN);

        Account account = validBuilder()
                .roles(List.of(role))
                .build();

        assertEquals(1, account.getRoles().size());
        assertRole(account.getRoles().get(0), ADMIN);
    }
}