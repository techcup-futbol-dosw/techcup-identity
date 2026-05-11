package edu.eci.dosw.dto;

import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.model.IdentificationType;

import java.util.List;

public class AccountAdminItemResponse {

    private Long id;
    private String name;
    private String lastName;
    private String fullName;
    private String email;

    private IdentificationType identificationType;
    private String identification;

    private AccountStatus status;

    private String program;
    private Integer semester;

    private List<String> roles;

    public AccountAdminItemResponse() {
    }

    public AccountAdminItemResponse(Long id,
                                    String name,
                                    String lastName,
                                    String fullName,
                                    String email,
                                    IdentificationType identificationType,
                                    String identification,
                                    AccountStatus status,
                                    String program,
                                    Integer semester,
                                    List<String> roles) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.fullName = fullName;
        this.email = email;
        this.identificationType = identificationType;
        this.identification = identification;
        this.status = status;
        this.program = program;
        this.semester = semester;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}