package edu.eci.dosw.exception;

public class RefreshTokenRevokedException extends BusinessException {

    public RefreshTokenRevokedException() {
        super("Refresh token revoked or invalid");
    }
}

