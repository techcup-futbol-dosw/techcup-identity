package edu.eci.dosw.exception;


public class AccountCannotBeDeactivatedException extends BusinessException {

    public AccountCannotBeDeactivatedException(Long accountId, Long teamId) {
        super("Account cannot be deactivated because account "
                + accountId
                + " belongs to team "
                + teamId
                + " enrolled in an active tournament");
    }
}
