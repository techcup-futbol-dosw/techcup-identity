package edu.eci.dosw.unitaria.mapper;

import edu.eci.dosw.entity.*;
import edu.eci.dosw.mapper.*;
import edu.eci.dosw.dto.AccountResponse;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountMapperTest {

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private AccountMapper accountMapper;

    @Test
    void toModel_ShouldReturnNull_WhenEntityIsNull() {
        assertNull(accountMapper.toModel(null));
    }

    @Test
    void toModel_ShouldMapAllFields_WhenEntityIsValid() {
        LocalDateTime now = LocalDateTime.now();
        RoleEntity roleEntity = new RoleEntity();
        Role role = new Role();
        role.setName("PLAYER");

        AccountEntity entity = new AccountEntity();
        entity.setId(1L);
        entity.setEmail("juan@escuelaing.edu.co");
        entity.setPassword("encoded-password");
        entity.setStatus(AccountStatus.ACTIVE);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setLastLoginAt(now);
        entity.setRoles(List.of(roleEntity));

        when(roleMapper.toModel(roleEntity)).thenReturn(role);

        Account result = accountMapper.toModel(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("juan@escuelaing.edu.co", result.getEmail());
        assertEquals("encoded-password", result.getPassword());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
        assertEquals(now, result.getLastLoginAt());
        assertEquals(1, result.getRoles().size());
        assertEquals("PLAYER", result.getRoles().get(0).getName());

        verify(roleMapper).toModel(roleEntity);
    }

    @Test
    void toModel_ShouldReturnEmptyRoles_WhenEntityHasNullRoles() {
        LocalDateTime now = LocalDateTime.now();
        AccountEntity entity = new AccountEntity();
        entity.setId(1L);
        entity.setEmail("juan@escuelaing.edu.co");
        entity.setPassword("encoded-password");
        entity.setStatus(AccountStatus.ACTIVE);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setRoles(null);

        Account result = accountMapper.toModel(entity);

        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());
        verifyNoInteractions(roleMapper);
    }

    @Test
    void toEntity_ShouldReturnNull_WhenModelIsNull() {
        assertNull(accountMapper.toEntity(null));
    }

    @Test
    void toEntity_ShouldMapAllFields_WhenModelIsValid() {
        LocalDateTime now = LocalDateTime.now();
        Role role = new Role();
        role.setName("PLAYER");
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName("PLAYER");

        Account model = new Account(1L, "juan@escuelaing.edu.co", "encoded-password",
                AccountStatus.ACTIVE, now, now, now, List.of(role));

        when(roleMapper.toEntity(role)).thenReturn(roleEntity);

        AccountEntity result = accountMapper.toEntity(model);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("juan@escuelaing.edu.co", result.getEmail());
        assertEquals("encoded-password", result.getPassword());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
        assertEquals(now, result.getLastLoginAt());
        assertEquals(1, result.getRoles().size());

        verify(roleMapper).toEntity(role);
    }

    @Test
    void toEntity_ShouldReturnEmptyRoles_WhenModelHasNullRoles() {
        LocalDateTime now = LocalDateTime.now();
        Account model = new Account(1L, "juan@escuelaing.edu.co", "encoded-password",
                AccountStatus.ACTIVE, now, now, null, null);

        AccountEntity result = accountMapper.toEntity(model);

        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());
        verifyNoInteractions(roleMapper);
    }

    @Test
    void toResponse_ShouldReturnNull_WhenModelIsNull() {
        assertNull(accountMapper.toResponse(null));
    }

    @Test
    void toResponse_ShouldMapAllFields_WhenModelIsValid() {
        LocalDateTime now = LocalDateTime.now();
        Role role = new Role();
        role.setName("PLAYER");

        Account model = new Account(1L, "juan@escuelaing.edu.co", "encoded-password",
                AccountStatus.ACTIVE, now, now, null, List.of(role));

        AccountResponse result = accountMapper.toResponse(model);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("juan@escuelaing.edu.co", result.getEmail());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(now, result.getCreatedAt());
        assertEquals(1, result.getRoles().size());
        assertEquals("PLAYER", result.getRoles().get(0));
    }

    @Test
    void toResponse_ShouldReturnEmptyRoles_WhenModelHasNullRoles() {
        LocalDateTime now = LocalDateTime.now();
        Account model = new Account(1L, "juan@escuelaing.edu.co", "encoded-password",
                AccountStatus.ACTIVE, now, now, null, null);

        AccountResponse result = accountMapper.toResponse(model);

        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());
    }
}