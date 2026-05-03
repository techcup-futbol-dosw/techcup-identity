package edu.eci.dosw.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountBuilder {

    private Long id;
    private String email;
    private String passwordHash;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private List<Role> roles = new ArrayList<>();

    public AccountBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public AccountBuilder email(String email) {
        this.email = email;
        return this;
    }

    public AccountBuilder passwordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public AccountBuilder status(String status) {
        this.status = status;
        return this;
    }

    public AccountBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public AccountBuilder updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public AccountBuilder lastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
        return this;
    }

    public AccountBuilder roles(List<Role> roles) {
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
        return this;

    }

    public AccountBuilder addRole(Role role) {
        if (role != null) {
            this.roles.add(role);
        }
        return this;
    }

    public Account build() {
        validateRequiredFields();
        setDefaultFields();
        return new Account(
                id,
                email,
                passwordHash,
                status,
                createdAt,
                updatedAt,
                lastLoginAt,
                new ArrayList<>(roles)
        );
    }

    private void validateRequiredFields() {
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email is required");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalStateException("Password hash is required");
        }

        if (createdAt == null) {
            throw new IllegalStateException("CreatedAt is required");
        }

        if (roles == null || roles.isEmpty()){
            throw  new IllegalStateException("At least one role is required");
        }
    }
    private void setDefaultFields(){
        if (status == null || status.isBlank()){
            status = "ACTIVE";
        }
        if (updatedAt == null || updatedAt.isBefore(createdAt)){
            updatedAt = createdAt;
        }
        if (lastLoginAt != null && lastLoginAt.isBefore(createdAt)){
            lastLoginAt = null;
        }
    }
}