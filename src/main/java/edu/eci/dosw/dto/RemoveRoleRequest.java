package edu.eci.dosw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RemoveRoleRequest {

    @NotNull
    private Long accountId;

    @NotBlank
    private String roleName;

    public RemoveRoleRequest() {}

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
