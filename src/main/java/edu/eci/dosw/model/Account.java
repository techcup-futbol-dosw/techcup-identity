package edu.eci.dosw.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Account {

    private Long id;
    private String name;
    private String lastName;
    private LocalDate birthDate;
    private Relation relation;
    private Integer semester;
    private String program;
    private String email;
    private String passwordHash;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private Gender gender;
    private IdentificationType identificationType;
    private String identification;
    private List<Role> roles;

    public Account(LocalDate birthDate, LocalDateTime createdAt, String email, Gender gender, Long id, String identification, IdentificationType identificationType, LocalDateTime lastLoginAt, String lastName, String name, String passwordHash, String program, Relation relation, List<Role> roles, Integer semester, AccountStatus status, LocalDateTime updatedAt) {
        this.birthDate = birthDate;
        this.createdAt = createdAt;
        this.email = email;
        this.gender = gender;
        this.id = id;
        this.identification = identification;
        this.identificationType = identificationType;
        this.lastLoginAt = lastLoginAt;
        this.lastName = lastName;
        this.name = name;
        this.passwordHash = passwordHash;
        this.program = program;
        this.relation = relation;
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
        this.semester = semester;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Account(LocalDate birthDate, LocalDateTime createdAt, String email, Gender gender, String identification, IdentificationType identificationType, LocalDateTime lastLoginAt, String lastName, String name, String passwordHash, String program, Relation relation, List<Role> roles, Integer semester, LocalDateTime updatedAt) {
        this.birthDate = birthDate;
        this.createdAt = createdAt;
        this.email = email;
        this.gender = gender;
        this.identification = identification;
        this.identificationType = identificationType;
        this.lastLoginAt = lastLoginAt;
        this.lastName = lastName;
        this.name = name;
        this.passwordHash = passwordHash;
        this.program = program;
        this.relation = relation;
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
        this.semester = semester;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public IdentificationType getIdentificationType() {
        return identificationType;
    }

    public void setIdentificationType(IdentificationType identificationType) {
        this.identificationType = identificationType;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

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