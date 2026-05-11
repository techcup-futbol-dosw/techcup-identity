package edu.eci.dosw.unitary.exception;

import org.junit.jupiter.api.Test;
import edu.eci.dosw.exception.*;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    void invalidRegistrationData_DefaultConstructor_ShouldSetDefaultMessage() {
        InvalidRegistrationDataException ex = new InvalidRegistrationDataException();
        assertEquals("Invalid registration data", ex.getMessage());
    }

    @Test
    void invalidRegistrationData_CustomConstructor_ShouldSetCustomMessage() {
        InvalidRegistrationDataException ex = new InvalidRegistrationDataException("Custom message");
        assertEquals("Custom message", ex.getMessage());
    }

    @Test
    void emailAlreadyRegistered_DefaultConstructor_ShouldSetDefaultMessage() {
        EmailAlreadyRegisteredException ex = new EmailAlreadyRegisteredException();
        assertEquals("Email already registered", ex.getMessage());
    }

    @Test
    void emailAlreadyRegistered_EmailConstructor_ShouldIncludeEmailInMessage() {
        EmailAlreadyRegisteredException ex = new EmailAlreadyRegisteredException("juan@escuelaing.edu.co");
        assertEquals("Email already registered: juan@escuelaing.edu.co", ex.getMessage());
    }

    @Test
    void accountAlreadyInactive_Constructor_ShouldIncludeAccountIdInMessage() {
        AccountAlreadyInactiveException ex = new AccountAlreadyInactiveException(1L);
        assertEquals("Account is already inactive: 1", ex.getMessage());
    }

    @Test
    void missingRequiredField_FieldNameConstructor_ShouldSetMessage() {
        MissingRequiredFieldException ex = new MissingRequiredFieldException("email");
        assertEquals("email is required", ex.getMessage());
    }

    @Test
    void missingRequiredField_FieldNameAndContextConstructor_ShouldSetMessage() {
        MissingRequiredFieldException ex = new MissingRequiredFieldException("semester", "students");
        assertEquals("semester is required for students", ex.getMessage());
    }
}