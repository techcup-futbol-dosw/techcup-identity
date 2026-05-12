package edu.eci.dosw.unitary;

import edu.eci.dosw.dto.*;
import edu.eci.dosw.exception.AccountNotFoundException;
import edu.eci.dosw.exception.RoleNotFoundException;
import edu.eci.dosw.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NewUnitaryTest {

    @Test
    void shouldCreateDefaultAccountNotFoundMessage() {

        AccountNotFoundException ex =
                new AccountNotFoundException();

        assertEquals(
                "Account not found",
                ex.getMessage()
        );
    }

    @Test
    void shouldCreateAccountNotFoundMessageWithId() {

        AccountNotFoundException ex =
                new AccountNotFoundException(1L);

        assertEquals(
                "Account not found with id: 1",
                ex.getMessage()
        );
    }

    @Test
    void shouldCreateAccountNotFoundMessageWithEmail() {

        AccountNotFoundException ex =
                new AccountNotFoundException("test@mail.com");

        assertEquals(
                "Account not found with email: test@mail.com",
                ex.getMessage()
        );
    }

    @Test
    void shouldCreateDefaultRoleNotFoundMessage() {

        RoleNotFoundException ex =
                new RoleNotFoundException();

        assertEquals(
                "Role not found",
                ex.getMessage()
        );
    }

    @Test
    void shouldAddRoleWhenRolesListIsEmpty() {

        Account account = buildAccount();

        Role role = new Role();
        role.setId(1L);

        account.addRole(role);

        assertEquals(
                1,
                account.getRoles().size()
        );
    }

    @Test
    void shouldNotAddDuplicateRole() {

        Account account = buildAccount();

        Role role = new Role();
        role.setId(1L);

        account.addRole(role);
        account.addRole(role);

        assertEquals(
                1,
                account.getRoles().size()
        );
    }

    @Test
    void shouldRemoveRoleSuccessfully() {

        Account account = buildAccount();

        Role role = new Role();
        role.setId(1L);

        account.addRole(role);

        account.removeRole(role);

        assertTrue(
                account.getRoles().isEmpty()
        );
    }

    @Test
    void shouldReturnTrueWhenPermissionExists() {

        Permission permission = new Permission();
        permission.setName("account:read");

        Role role = new Role();
        role.setPermissions(List.of(permission));

        assertTrue(
                role.hasPermission("account:read")
        );
    }

    @Test
    void shouldIgnoreCaseInPermissionValidation() {

        Permission permission = new Permission();
        permission.setName("ACCOUNT:READ");

        Role role = new Role();
        role.setPermissions(List.of(permission));

        assertTrue(
                role.hasPermission("account:read")
        );
    }

    @Test
    void shouldCreateAccountAdminItemResponseUsingConstructor() {

        AccountAdminItemResponse response =
                new AccountAdminItemResponse(
                        1L,
                        "Juan",
                        "Perez",
                        "Juan Perez",
                        "juan@mail.com",
                        IdentificationType.CC,
                        "123",
                        AccountStatus.ACTIVE,
                        "Systems",
                        8,
                        List.of("ADMIN")
                );

        assertEquals("Juan", response.getName());
        assertEquals("Perez", response.getLastName());
        assertEquals("Systems", response.getProgram());
    }

    @Test
    void shouldSetAndGetFieldsCorrectly() {

        AccountAdminItemResponse response =
                new AccountAdminItemResponse();

        response.setProgram("Engineering");
        response.setSemester(9);

        assertEquals(
                "Engineering",
                response.getProgram()
        );

        assertEquals(
                9,
                response.getSemester()
        );
    }

    private Account buildAccount() {

        return new Account(
                LocalDate.now(),
                LocalDateTime.now(),
                "test@mail.com",
                Gender.MALE,
                1L,
                "123456",
                IdentificationType.CC,
                LocalDateTime.now(),
                "Perez",
                "Juan",
                "password",
                "Systems",
                Relation.ESTUDIANTE,
                null,
                8,
                AccountStatus.ACTIVE,
                LocalDateTime.now()
        );
    }
}