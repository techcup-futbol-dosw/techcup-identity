package edu.eci.dosw.unitaria.mapper;

import edu.eci.dosw.dto.AccountResponse;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.exception.InvalidAccountBuildException;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.model.Gender;
import edu.eci.dosw.model.IdentificationType;
import edu.eci.dosw.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        roleEntity.setId(1L);
        roleEntity.setName("PLAYER");

        Role role = new Role();
        role.setId(1L);
        role.setName("PLAYER");

        AccountEntity entity = createValidEntity(now);
        entity.setRoles(List.of(roleEntity));

        when(roleMapper.toModel(roleEntity)).thenReturn(role);

        Account result = accountMapper.toModel(entity);

        assertNotNull(result);

        assertEquals(1L, result.getId());
        assertEquals("Juan", result.getName());
        assertEquals("Roa", result.getLastName());
        assertEquals(LocalDate.of(2000, 5, 15), result.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, result.getRelation());
        assertEquals(7, result.getSemester());
        assertEquals("INGENIERIA_SISTEMAS", result.getProgram());

        assertEquals("juan@escuelaing.edu.co", result.getEmail());
        assertEquals("encoded-password", result.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());

        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
        assertEquals(now, result.getLastLoginAt());

        assertEquals(Gender.MALE, result.getGender());
        assertEquals(IdentificationType.CC, result.getIdentificationType());
        assertEquals("123456789", result.getIdentification());

        assertEquals(1, result.getRoles().size());
        assertEquals("PLAYER", result.getRoles().get(0).getName());

        verify(roleMapper).toModel(roleEntity);
    }

    @Test
    void toModel_ShouldThrowException_WhenEntityHasNullRoles() {
        LocalDateTime now = LocalDateTime.now();

        AccountEntity entity = createValidEntity(now);
        entity.setRoles(null);

        assertThrows(InvalidAccountBuildException.class, () -> accountMapper.toModel(entity));

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
        role.setId(1L);
        role.setName("PLAYER");

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(1L);
        roleEntity.setName("PLAYER");

        Account model = createValidModel(now, List.of(role));

        when(roleMapper.toEntity(role)).thenReturn(roleEntity);

        AccountEntity result = accountMapper.toEntity(model);

        assertNotNull(result);

        assertEquals(1L, result.getId());
        assertEquals("Juan", result.getName());
        assertEquals("Roa", result.getLastName());
        assertEquals(LocalDate.of(2000, 5, 15), result.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, result.getRelation());
        assertEquals(7, result.getSemester());
        assertEquals("INGENIERIA_SISTEMAS", result.getProgram());

        assertEquals("juan@escuelaing.edu.co", result.getEmail());
        assertEquals("encoded-password", result.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());

        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
        assertEquals(now, result.getLastLoginAt());

        assertEquals(Gender.MALE, result.getGender());
        assertEquals(IdentificationType.CC, result.getIdentificationType());
        assertEquals("123456789", result.getIdentification());

        assertEquals(1, result.getRoles().size());
        assertEquals("PLAYER", result.getRoles().get(0).getName());

        verify(roleMapper).toEntity(role);
    }

    @Test
    void toResponse_ShouldReturnNull_WhenModelIsNull() {
        assertNull(accountMapper.toResponse(null));
    }

    @Test
    void toResponse_ShouldMapAllFields_WhenModelIsValid() {
        LocalDateTime now = LocalDateTime.now();

        Role role = new Role();
        role.setId(1L);
        role.setName("PLAYER");

        Account model = createValidModel(now, List.of(role));

        AccountResponse result = accountMapper.toResponse(model);

        assertNotNull(result);

        assertEquals(1L, result.getId());
        assertEquals("juan@escuelaing.edu.co", result.getEmail());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(now, result.getCreatedAt());

        assertEquals("Juan", result.getName());
        assertEquals("Roa", result.getLastName());
        assertEquals(LocalDate.of(2000, 5, 15), result.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, result.getRelation());
        assertEquals(7, result.getSemester());
        assertEquals("INGENIERIA_SISTEMAS", result.getProgram());

        assertEquals(Gender.MALE, result.getGender());
        assertEquals(IdentificationType.CC, result.getIdentificationType());
        assertEquals("123456789", result.getIdentification());

        assertEquals(1, result.getRoles().size());
        assertEquals("PLAYER", result.getRoles().get(0));
    }

    private AccountEntity createValidEntity(LocalDateTime now) {
        AccountEntity entity = new AccountEntity();

        entity.setId(1L);
        entity.setName("Juan");
        entity.setLastName("Roa");
        entity.setBirthDate(LocalDate.of(2000, 5, 15));
        entity.setRelation(Relation.ESTUDIANTE);
        entity.setSemester(7);
        entity.setProgram("INGENIERIA_SISTEMAS");

        entity.setEmail("juan@escuelaing.edu.co");
        entity.setPasswordHash("encoded-password");
        entity.setStatus(AccountStatus.ACTIVE);

        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setLastLoginAt(now);

        entity.setGender(Gender.MALE);
        entity.setIdentificationType(IdentificationType.CC);
        entity.setIdentification("123456789");

        return entity;
    }

    private Account createValidModel(LocalDateTime now, List<Role> roles) {
        return new AccountBuilder()
                .id(1L)
                .name("Juan")
                .lastName("Roa")
                .birthDate(LocalDate.of(2000, 5, 15))
                .relation(Relation.ESTUDIANTE)
                .semester(7)
                .program("INGENIERIA_SISTEMAS")
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .status(AccountStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .lastLoginAt(now)
                .gender(Gender.MALE)
                .identificationType(IdentificationType.CC)
                .identification("123456789")
                .roles(roles)
                .build();
    }
}