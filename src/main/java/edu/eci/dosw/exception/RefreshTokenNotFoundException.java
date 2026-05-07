package edu.eci.dosw.exception;

public class RefreshTokenNotFoundException extends BusinessException {

  public RefreshTokenNotFoundException() {
    super("Refresh token not found");
  }
}
