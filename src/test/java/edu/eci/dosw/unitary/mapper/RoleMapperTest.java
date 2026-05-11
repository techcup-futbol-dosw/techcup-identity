package edu.eci.dosw.unitary.mapper;

import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.*;
import edu.eci.dosw.model.Permission;
import edu.eci.dosw.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleMapperTest {

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private RoleMapper roleMapper;

    @Test
    void toModel_ShouldReturnNull_WhenEntityIsNull() {
        assertNull(roleMapper.toModel(null));
    }

    @Test
    void toModel_ShouldMapAllFields_WhenEntityIsValid() {
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setId(1L);
        permissionEntity.setName("tournament:read");

        Permission permission = new Permission(1L, "tournament:read");

        RoleEntity entity = new RoleEntity();
        entity.setId(1L);
        entity.setName("PLAYER");
        entity.setPermissions(List.of(permissionEntity));

        when(permissionMapper.toModel(permissionEntity)).thenReturn(permission);

        Role result = roleMapper.toModel(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PLAYER", result.getName());
        assertEquals(1, result.getPermissions().size());
        assertEquals("tournament:read", result.getPermissions().get(0).getName());

        verify(permissionMapper).toModel(permissionEntity);
    }

    @Test
    void toModel_ShouldReturnEmptyPermissions_WhenEntityHasNullPermissions() {
        RoleEntity entity = new RoleEntity();
        entity.setId(1L);
        entity.setName("PLAYER");
        entity.setPermissions(null);

        Role result = roleMapper.toModel(entity);

        assertNotNull(result);
        assertTrue(result.getPermissions().isEmpty());
        verifyNoInteractions(permissionMapper);
    }

    @Test
    void toEntity_ShouldReturnNull_WhenModelIsNull() {
        assertNull(roleMapper.toEntity(null));
    }

    @Test
    void toEntity_ShouldMapAllFields_WhenModelIsValid() {
        Permission permission = new Permission(1L, "tournament:read");
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setId(1L);
        permissionEntity.setName("tournament:read");

        Role model = new Role(1L, "PLAYER", List.of(permission));

        when(permissionMapper.toEntity(permission)).thenReturn(permissionEntity);

        RoleEntity result = roleMapper.toEntity(model);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PLAYER", result.getName());
        assertEquals(1, result.getPermissions().size());
        assertEquals("tournament:read", result.getPermissions().get(0).getName());

        verify(permissionMapper).toEntity(permission);
    }

    @Test
    void toEntity_ShouldReturnEmptyPermissions_WhenModelHasNullPermissions() {
        Role model = new Role(1L, "PLAYER", null);

        RoleEntity result = roleMapper.toEntity(model);

        assertNotNull(result);
        assertTrue(result.getPermissions().isEmpty());
        verifyNoInteractions(permissionMapper);
    }
}