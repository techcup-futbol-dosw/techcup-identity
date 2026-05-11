package edu.eci.dosw.mapper;

import edu.eci.dosw.dto.AccountAdminItemResponse;
import edu.eci.dosw.dto.AccountResponse;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.model.Role;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AccountMapper {

    private final RoleMapper roleMapper;

    public AccountMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public Account toModel(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        List<Role> roles = new ArrayList<>();
        if (entity.getRoles() != null) {
            for (RoleEntity roleEntity : entity.getRoles()) {
                roles.add(roleMapper.toModel(roleEntity));
            }
        }

        return new AccountBuilder()
                .id(entity.getId())
                .name(entity.getName())
                .lastName(entity.getLastName())
                .birthDate(entity.getBirthDate())
                .relation(entity.getRelation())
                .semester(entity.getSemester())
                .program(entity.getProgram())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lastLoginAt(entity.getLastLoginAt())
                .gender(entity.getGender())
                .identificationType(entity.getIdentificationType())
                .identification(entity.getIdentification())
                .roles(roles)
                .build();
    }

    public AccountEntity toEntity(Account model) {
        if (model == null) {
            return null;
        }

        AccountEntity entity = new AccountEntity();

        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setLastName(model.getLastName());
        entity.setBirthDate(model.getBirthDate());
        entity.setRelation(model.getRelation());
        entity.setSemester(model.getSemester());
        entity.setProgram(model.getProgram());
        entity.setEmail(model.getEmail());
        entity.setPasswordHash(model.getPasswordHash());
        entity.setStatus(model.getStatus());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());
        entity.setLastLoginAt(model.getLastLoginAt());
        entity.setGender(model.getGender());
        entity.setIdentificationType(model.getIdentificationType());
        entity.setIdentification(model.getIdentification());

        List<RoleEntity> roleEntities = new ArrayList<>();
        if (model.getRoles() != null) {
            for (Role role : model.getRoles()) {
                roleEntities.add(roleMapper.toEntity(role));
            }
        }

        entity.setRoles(roleEntities);

        return entity;
    }

    public AccountResponse toResponse(Account model) {
        if (model == null) {
            return null;
        }

        List<String> roleNames = new ArrayList<>();
        if (model.getRoles() != null) {
            for (Role role : model.getRoles()) {
                roleNames.add(role.getName());
            }
        }

        return new AccountResponse(
                model.getCreatedAt(),
                model.getBirthDate(),
                model.getEmail(),
                model.getGender(),
                model.getId(),
                model.getIdentification(),
                model.getIdentificationType(),
                model.getLastName(),
                model.getName(),
                model.getProgram(),
                model.getRelation(),
                roleNames,
                model.getSemester(),
                model.getStatus()
        );
    }
    public AccountAdminItemResponse toAdminItemResponse(Account model) {
        AccountAdminItemResponse response = new AccountAdminItemResponse();

        response.setId(model.getId());
        response.setName(model.getName());
        response.setLastName(model.getLastName());
        response.setFullName(model.getName() + " " + model.getLastName());
        response.setEmail(model.getEmail());

        response.setIdentificationType(model.getIdentificationType());
        response.setIdentification(model.getIdentification());

        response.setStatus(model.getStatus());

        response.setProgram(model.getProgram());
        response.setSemester(model.getSemester());

        List<String> roleNames = model.getRoles() == null
                ? List.of()
                : model.getRoles().stream()
                .map(Role::getName)
                .toList();

        response.setRoles(roleNames);

        return response;
    }
}