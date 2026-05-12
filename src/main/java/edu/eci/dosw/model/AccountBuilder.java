package edu.eci.dosw.model;

import edu.eci.dosw.exception.InvalidAccountBuildException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountBuilder {

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

    private List<Role> roles = new ArrayList<>();

    public AccountBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public AccountBuilder name(String name) {
        this.name = name;
        return this;
    }

    public AccountBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public AccountBuilder birthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
        return this;
    }

    public AccountBuilder relation(Relation relation) {
        this.relation = relation;
        return this;
    }

    public AccountBuilder semester(Integer semester) {
        this.semester = semester;
        return this;
    }

    public AccountBuilder program(String program) {
        this.program = program;
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

    public AccountBuilder status(AccountStatus status) {
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

    public AccountBuilder gender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public AccountBuilder identificationType(IdentificationType identificationType) {
        this.identificationType = identificationType;
        return this;
    }

    public AccountBuilder identification(String identification) {
        this.identification = identification;
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
                birthDate,
                createdAt,
                email,
                gender,
                id,
                identification,
                identificationType,
                lastLoginAt,
                lastName,
                name,
                passwordHash,
                program,
                relation,
                new ArrayList<>(roles),
                semester,
                status,
                updatedAt
        );
    }

    private void validateRequiredFields() {
        requireNotBlank(name, "Name is required");
        requireNotBlank(lastName, "Last name is required");
        requireNotNull(birthDate, "Birth date is required");
        requireNotNull(relation, "Relation is required");
        if (relation != null && relation.equals(Relation.ESTUDIANTE)) {
            requireNotNull(semester, "Semester is required");
        }
        requireNotBlank(program, "Program is required");
        requireNotBlank(email, "Email is required");
        requireNotBlank(passwordHash, "Password hash is required");
        requireNotNull(createdAt, "CreatedAt is required");
        requireNotNull(gender, "Gender is required");
        requireNotNull(identificationType, "Identification type is required");
        requireNotBlank(identification, "Identification is required");
        requireNotEmpty(roles, "At least one role is required");
    }

    private void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidAccountBuildException(message);
        }
    }

    private void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new InvalidAccountBuildException(message);
        }
    }


    private void requireNotEmpty(List<?> value, String message) {
        if (value == null || value.isEmpty()) {
            throw new InvalidAccountBuildException(message);
        }
    }

            private void setDefaultFields() {
        if (status == null) {
            status = AccountStatus.ACTIVE;
        }

        if (updatedAt == null || updatedAt.isBefore(createdAt)) {
            updatedAt = createdAt;
        }

        if (lastLoginAt != null && lastLoginAt.isBefore(createdAt)) {
            lastLoginAt = null;
        }
    }
}
