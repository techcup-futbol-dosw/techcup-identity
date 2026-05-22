package edu.eci.dosw.unitary.dto;

import edu.eci.dosw.dto.AccountResponse;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.model.Program;
import edu.eci.dosw.model.Gender;
import edu.eci.dosw.model.IdentificationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountResponseTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithNullFields() {
        AccountResponse response = new AccountResponse();

        assertNull(response.getId());
        assertNull(response.getEmail());
        assertNull(response.getStatus());
        assertNull(response.getCreatedAt());
        assertNull(response.getRoles());

        assertNull(response.getName());
        assertNull(response.getLastName());
        assertNull(response.getBirthDate());
        assertNull(response.getRelation());
        assertNull(response.getSemester());
        assertNull(response.getProgram());
        assertNull(response.getGender());
        assertNull(response.getIdentificationType());
        assertNull(response.getIdentification());
    }

    @Test
    void parameterizedConstructor_ShouldSetAllFields() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDate birthDate = LocalDate.of(2000, 5, 15);
        List<String> roles = List.of("PLAYER");

        AccountResponse response = new AccountResponse(
                createdAt,
                birthDate,
                "juan@escuelaing.edu.co",
                Gender.MALE,
                1L,
                "123456789",
                IdentificationType.CC,
                "Roa",
                "Juan",
                Program.SISTEMAS,
                Relation.ESTUDIANTE,
                roles,
                7,
                AccountStatus.ACTIVE
        );

        assertEquals(1L, response.getId());
        assertEquals("juan@escuelaing.edu.co", response.getEmail());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(roles, response.getRoles());

        assertEquals("Juan", response.getName());
        assertEquals("Roa", response.getLastName());
        assertEquals(birthDate, response.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, response.getRelation());
        assertEquals(7, response.getSemester());
        assertEquals(Program.SISTEMAS, response.getProgram());
        assertEquals(Gender.MALE, response.getGender());
        assertEquals(IdentificationType.CC, response.getIdentificationType());
        assertEquals("123456789", response.getIdentification());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDate birthDate = LocalDate.of(1999, 8, 20);
        List<String> roles = List.of("ADMIN");

        AccountResponse response = getAccountResponse(createdAt, roles, birthDate);

        assertEquals(1L, response.getId());
        assertEquals("juan@escuelaing.edu.co", response.getEmail());
        assertEquals(AccountStatus.INACTIVE, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(roles, response.getRoles());

        assertEquals("Juan", response.getName());
        assertEquals("Roa", response.getLastName());
        assertEquals(birthDate, response.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, response.getRelation());
        assertEquals(8, response.getSemester());
        assertEquals(Program.SISTEMAS, response.getProgram());
        assertEquals(Gender.MALE, response.getGender());
        assertEquals(IdentificationType.CC, response.getIdentificationType());
        assertEquals("987654321", response.getIdentification());
    }

    private static AccountResponse getAccountResponse(LocalDateTime createdAt, List<String> roles, LocalDate birthDate) {
        AccountResponse response = new AccountResponse();

        response.setId(1L);
        response.setEmail("juan@escuelaing.edu.co");
        response.setStatus(AccountStatus.INACTIVE);
        response.setCreatedAt(createdAt);
        response.setRoles(roles);

        response.setName("Juan");
        response.setLastName("Roa");
        response.setBirthDate(birthDate);
        response.setRelation(Relation.ESTUDIANTE);
        response.setSemester(8);
        response.setProgram(Program.SISTEMAS);
        response.setGender(Gender.MALE);
        response.setIdentificationType(IdentificationType.CC);
        response.setIdentification("987654321");
        return response;
    }
}