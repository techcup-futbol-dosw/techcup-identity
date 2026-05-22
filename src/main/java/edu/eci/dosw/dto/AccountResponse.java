package edu.eci.dosw.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.model.Program;
import edu.eci.dosw.model.Gender;
import edu.eci.dosw.model.IdentificationType;
import edu.eci.dosw.model.Relation;

public class AccountResponse {

    private Long id;
    private String email;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private List<String> roles;
    private String name;
    private String lastName;
    private LocalDate birthDate;
    private Relation relation;
    private Integer semester;
    private Program program;
    private Gender gender;
    private IdentificationType identificationType;
    private String identification;

    public AccountResponse() {}

    public AccountResponse(LocalDateTime createdAt, LocalDate birthDate, String email, Gender gender, Long id, String identification, IdentificationType identificationType, String lastName, String name, Program program, Relation relation, List<String> roles, Integer semester, AccountStatus status) {
        this.createdAt = createdAt;
        this.birthDate = birthDate;
        this.email = email;
        this.gender = gender;
        this.id = id;
        this.identification = identification;
        this.identificationType = identificationType;
        this.lastName = lastName;
        this.name = name;
        this.program = program;
        this.relation = relation;
        this.roles = roles;
        this.semester = semester;
        this.status = status;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public IdentificationType getIdentificationType() {
        return identificationType;
    }

    public void setIdentificationType(IdentificationType identificationType) {
        this.identificationType = identificationType;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
