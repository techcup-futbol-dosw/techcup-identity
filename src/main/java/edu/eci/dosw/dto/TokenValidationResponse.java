package edu.eci.dosw.dto;

import java.util.List;

public class TokenValidationResponse {

    private boolean valid;
    private String accountId;
    private List<String> roles;
    private List<String> permissions;
    private String tokenType;

    public TokenValidationResponse() {}

    public TokenValidationResponse(boolean valid, String accountId,
                                   List<String> roles, List<String> permissions,
                                   String tokenType) {
        this.valid = valid;
        this.accountId = accountId;
        this.roles = roles;
        this.permissions = permissions;
        this.tokenType = tokenType;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}
