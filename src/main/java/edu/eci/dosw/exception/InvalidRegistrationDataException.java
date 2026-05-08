package edu.eci.dosw.exception;

public class InvalidRegistrationDataException extends BusinessException {

    public InvalidRegistrationDataException() {
        super("Invalid registration data");
    }

    public InvalidRegistrationDataException(String message) {
        super(message);
    }
}
