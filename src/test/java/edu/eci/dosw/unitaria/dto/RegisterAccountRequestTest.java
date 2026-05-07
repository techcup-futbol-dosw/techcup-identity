package edu.eci.dosw.unitaria.dto;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.dto.Program;
import edu.eci.dosw.dto.RegisterAccountRequest;
import edu.eci.dosw.dto.Relation;

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
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("password123");
        request.setRelation(Relation.ESTUDIANTE);
        request.setProgram(Program.SISTEMAS);
        request.setSemester(3);

        assertEquals("juan@escuelaing.edu.co", request.getEmail());
        assertEquals("password123", request.getPassword());
        assertEquals(Relation.ESTUDIANTE, request.getRelation());
        assertEquals(Program.SISTEMAS, request.getProgram());
        assertEquals(3, request.getSemester());
    }
}