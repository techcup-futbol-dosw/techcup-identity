package edu.eci.dosw.unitary.mapper;

import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.mapper.PermissionMapper;
import edu.eci.dosw.model.Permission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionMapperTest {

    private final PermissionMapper permissionMapper = new PermissionMapper();

    @Test
    void toModel_ShouldReturnNull_WhenEntityIsNull() {
        assertNull(permissionMapper.toModel(null));
    }

    @Test
    void toModel_ShouldMapAllFields_WhenEntityIsValid() {
        PermissionEntity entity = new PermissionEntity();
        entity.setId(1L);
        entity.setName("tournament:read");

        Permission result = permissionMapper.toModel(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("tournament:read", result.getName());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenModelIsNull() {
        assertNull(permissionMapper.toEntity(null));
    }

    @Test
    void toEntity_ShouldMapAllFields_WhenModelIsValid() {
        Permission model = new Permission(1L, "tournament:read");

        PermissionEntity result = permissionMapper.toEntity(model);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("tournament:read", result.getName());
    }
}