package edu.eci.dosw.testutil;

import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.model.Program;
import edu.eci.dosw.dto.RegisterAccountRequest;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static RegisterAccountRequest validRegisterRequest(String email) {
        RegisterAccountRequest request = new RegisterAccountRequest();

        request.setEmail(email);
        request.setPassword("Password123*");
        request.setRelation(Relation.ESTUDIANTE);
        request.setProgram(Program.SISTEMAS);
        request.setSemester(7);
        request.setName("Juan");
        request.setLastName("Roa");
        request.setBirthDate(LocalDate.of(2000, 5, 15));
        request.setGender(Gender.MALE);
        request.setIdentificationType(IdentificationType.CC);
        request.setIdentification(uniqueIdentification(email));

        return request;
    }

    public static AccountBuilder validAccountBuilder(String email) {
        return new AccountBuilder()
                .id(1L)
                .name("Juan")
                .lastName("Roa")
                .birthDate(LocalDate.of(2000, 5, 15))
                .relation(Relation.ESTUDIANTE)
                .semester(7)
                .program("SISTEMAS")
                .email(email)
                .passwordHash("encoded-password")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .gender(Gender.MALE)
                .identificationType(IdentificationType.CC)
                .identification(uniqueIdentification(email));
    }

    public static Account validAccount(String email, Role... roles) {
        AccountBuilder builder = validAccountBuilder(email);

        for (Role role : roles) {
            builder.addRole(role);
        }

        return builder.build();
    }

    public static Role role(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setPermissions(List.of());
        return role;
    }

    public static Role playerRole() {
        return role(1L, "PLAYER");
    }

    public static Role adminRole() {
        return role(2L, "ADMIN");
    }

    public static RoleEntity roleEntity(String name) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(name);
        roleEntity.setPermissions(new ArrayList<>());
        return roleEntity;
    }

    public static String uniqueIdentification(String seed) {
        return "ID-" + Math.abs(seed.hashCode());
    }
    public static AccountEntity validAccountEntity(String email) {
        AccountEntity entity = new AccountEntity();

        entity.setId(1L);
        entity.setName("Juan");
        entity.setLastName("Roa");
        entity.setBirthDate(LocalDate.of(2000, 5, 15));
        entity.setRelation(Relation.ESTUDIANTE);
        entity.setSemester(7);
        entity.setProgram("SISTEMAS");
        entity.setEmail(email);
        entity.setPasswordHash("encoded-password");
        entity.setStatus(AccountStatus.ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setGender(Gender.MALE);
        entity.setIdentificationType(IdentificationType.CC);
        entity.setIdentification(uniqueIdentification(email));

        return entity;
    }
}