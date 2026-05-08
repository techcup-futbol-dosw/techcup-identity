package edu.eci.dosw.dto;

import java.time.LocalDateTime;
import java.util.List;

import edu.eci.dosw.entity.AccountStatus;

public class AccountResponse {

    private Long id;
    private String email;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private List<String> roles;

    public AccountResponse() {}

    public AccountResponse(Long id, String email, AccountStatus status,
                           LocalDateTime createdAt, List<String> roles) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
