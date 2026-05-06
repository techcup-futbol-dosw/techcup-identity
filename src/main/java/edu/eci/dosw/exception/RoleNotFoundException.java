package edu.eci.dosw.exception;

public class RoleNotFoundException extends BusinessException {

    public RoleNotFoundException() {
        super("Role not found");
    }

    public RoleNotFoundException(String roleName) {
        super("Role not found: " + roleName);
    }
}