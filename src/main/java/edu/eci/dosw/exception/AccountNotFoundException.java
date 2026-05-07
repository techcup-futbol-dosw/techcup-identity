
package edu.eci.dosw.exception;

public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException() {
        super("Account not found");
    }

    public AccountNotFoundException(Long accountId) {
        super("Account not found with id: " + accountId);
    }
}
