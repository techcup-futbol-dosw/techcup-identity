package edu.eci.dosw.exception;

public class EmailAlreadyRegisteredException extends BusinessException {

    public EmailAlreadyRegisteredException() {
        super("Email already registered");
    }

    public EmailAlreadyRegisteredException(String email) {
        super("Email already registered: " + email);
    }
}