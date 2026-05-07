package edu.eci.dosw.exception;


public class InvalidRefreshTokenException extends BusinessException {

    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token");
    }
}

