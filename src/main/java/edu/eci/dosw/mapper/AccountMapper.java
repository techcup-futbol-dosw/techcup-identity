package edu.eci.dosw.mapper;

import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.model.Account;
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

    /** AccountEntity → Account (modelo de dominio) */
    public Account toModel(AccountEntity entity) {
        if (entity == null) return null;

        List<Role> roles = new ArrayList<>();
        if (entity.getRoles() != null) {
            for (RoleEntity roleEntity : entity.getRoles()) {
                roles.add(roleMapper.toModel(roleEntity));
            }
        }

        return new Account(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastLoginAt(),
                roles
        );
    }

    /** Account (modelo de dominio) → AccountEntity */
    public AccountEntity toEntity(Account model) {
        if (model == null) return null;

        AccountEntity entity = new AccountEntity();
        entity.setId(model.getId());
        entity.setEmail(model.getEmail());
        entity.setPassword(model.getPassword());
        entity.setStatus(model.getStatus());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());
        entity.setLastLoginAt(model.getLastLoginAt());

        List<RoleEntity> roleEntities = new ArrayList<>();
        if (model.getRoles() != null) {
            for (Role role : model.getRoles()) {
                roleEntities.add(roleMapper.toEntity(role));
            }
        }
        entity.setRoles(roleEntities);

        return entity;
    }
}
