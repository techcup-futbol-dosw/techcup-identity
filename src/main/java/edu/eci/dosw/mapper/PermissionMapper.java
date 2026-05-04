package edu.eci.dosw.mapper;

import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.model.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    /** PermissionEntity → Permission (modelo de dominio) */
    public Permission toModel(PermissionEntity entity) {
        if (entity == null) return null;
        return new Permission(entity.getId(), entity.getName());
    }

    /** Permission (modelo de dominio) → PermissionEntity */
    public PermissionEntity toEntity(Permission model) {
        if (model == null) return null;
        PermissionEntity entity = new PermissionEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        return entity;
    }
}
