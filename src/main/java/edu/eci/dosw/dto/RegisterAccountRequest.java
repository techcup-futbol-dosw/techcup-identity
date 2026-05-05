package edu.eci.dosw.dto;

import jakarta.validation.constraints.*;

public class RegisterAccountRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String password;

    @NotNull
    private Relation relation;

    private Program program;

    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 10, message = "Semester must be at most 10")
    private Integer semester;

    public RegisterAccountRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Relation getRelation() { return relation; }
    public void setRelation(Relation relation) { this.relation = relation; }

    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
}