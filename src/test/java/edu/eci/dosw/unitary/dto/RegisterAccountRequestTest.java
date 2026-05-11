package edu.eci.dosw.unitary.dto;

import edu.eci.dosw.model.Program;
import edu.eci.dosw.dto.RegisterAccountRequest;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.Gender;
import edu.eci.dosw.model.IdentificationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RegisterAccountRequestTest {

    @Test
    void defaultConstructor_ShouldCreateInstanceWithNullFields() {
        RegisterAccountRequest request = new RegisterAccountRequest();

        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getRelation());
        assertNull(request.getProgram());
        assertNull(request.getSemester());

        assertNull(request.getName());
        assertNull(request.getLastName());
        assertNull(request.getBirthDate());
        assertNull(request.getGender());
        assertNull(request.getIdentificationType());
        assertNull(request.getIdentification());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        LocalDate birthDate = LocalDate.of(2000, 5, 15);
        RegisterAccountRequest request = new RegisterAccountRequest();

        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("password123");
        request.setRelation(Relation.ESTUDIANTE);
        request.setProgram(Program.SISTEMAS);
        request.setSemester(3);

        request.setName("Juan");
        request.setLastName("Roa");
        request.setBirthDate(birthDate);
        request.setGender(Gender.MALE);
        request.setIdentificationType(IdentificationType.CC);
        request.setIdentification("123456789");

        assertEquals("juan@escuelaing.edu.co", request.getEmail());
        assertEquals("password123", request.getPassword());
        assertEquals(Relation.ESTUDIANTE, request.getRelation());
        assertEquals(Program.SISTEMAS, request.getProgram());
        assertEquals(3, request.getSemester());

        assertEquals("Juan", request.getName());
        assertEquals("Roa", request.getLastName());
        assertEquals(birthDate, request.getBirthDate());
        assertEquals(Gender.MALE, request.getGender());
        assertEquals(IdentificationType.CC, request.getIdentificationType());
        assertEquals("123456789", request.getIdentification());
    }
}