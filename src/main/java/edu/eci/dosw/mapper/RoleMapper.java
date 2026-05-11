package edu.eci.dosw.mapper;

import edu.eci.dosw.dto.RoleSummaryResponse;
import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.model.Permission;
import edu.eci.dosw.model.Role;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /** RoleEntity → Role (modelo de dominio) */
    public Role toModel(RoleEntity entity) {
        if (entity == null) return null;

        List<Permission> permissions = new ArrayList<>();
        if (entity.getPermissions() != null) {
            for (PermissionEntity permEntity : entity.getPermissions()) {
                permissions.add(permissionMapper.toModel(permEntity));
            }
        }

        return new Role(entity.getId(), entity.getName(), permissions);
    }

    /** Role (modelo de dominio) → RoleEntity */
    public RoleEntity toEntity(Role model) {
        if (model == null) return null;

        RoleEntity entity = new RoleEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());

        List<PermissionEntity> permEntities = new ArrayList<>();
        if (model.getPermissions() != null) {
            for (Permission perm : model.getPermissions()) {
                permEntities.add(permissionMapper.toEntity(perm));
            }
        }
        entity.setPermissions(permEntities);

        return entity;
    }
    public RoleSummaryResponse toSummaryResponse(Role model) {
        if (model == null) {
            return null;
        }

        return new RoleSummaryResponse(
                model.getId(),
                model.getName()
        );
    }
}
