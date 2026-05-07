package edu.eci.dosw.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.eci.dosw.entity.AccountStatus;

public class Account {

    private Long id;
    private String email;
    private String password;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private List<Role> roles;

    public Account(Long id, String email, String password, AccountStatus status,
                   LocalDateTime createdAt, LocalDateTime updatedAt,
                   LocalDateTime lastLoginAt, List<Role> roles) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>(); }

    public void addRole(Role role) {
        if (this.roles == null) this.roles = new ArrayList<>();
        boolean alreadyExists = this.roles.stream().anyMatch(r -> r.getId().equals(role.getId()));
        if (!alreadyExists) {
            this.roles.add(role);
        }
    }

    public void removeRole(Role role) {
        if (this.roles == null || role == null || role.getId() == null) return;
        this.roles.removeIf(r -> r.getId().equals(role.getId()));
    }

    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status);
    }
}