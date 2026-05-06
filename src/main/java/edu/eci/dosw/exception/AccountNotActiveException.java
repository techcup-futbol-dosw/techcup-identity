package edu.eci.dosw.exception;


public class AccountNotActiveException extends BusinessException {

  public AccountNotActiveException(Long accountId) {
    super("Account is not active: " + accountId);
  }
}

