package edu.eci.dosw.exception;

public class AccountAlreadyInactiveException extends BusinessException {

    public AccountAlreadyInactiveException(Long accountId) {
        super("Account is already inactive: " + accountId);
    }
}