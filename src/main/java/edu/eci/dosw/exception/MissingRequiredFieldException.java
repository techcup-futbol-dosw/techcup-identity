package edu.eci.dosw.exception;

public class MissingRequiredFieldException extends BusinessException {

    public MissingRequiredFieldException(String fieldName) {
        super(fieldName + " is required");
    }

    public MissingRequiredFieldException(String fieldName, String context) {
        super(fieldName + " is required for " + context);
    }
}
