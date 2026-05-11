package edu.eci.dosw.unitary.mapper;
import edu.eci.dosw.dto.AccountResponse;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.exception.InvalidAccountBuildException;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.model.Gender;
import edu.eci.dosw.model.IdentificationType;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static edu.eci.dosw.testutil.TestDataFactory.validAccountBuilder;
import static edu.eci.dosw.testutil.TestDataFactory.validAccountEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountMapperTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long ROLE_ID = 1L;

    private static final String EMAIL = "juan@escuelaing.edu.co";
    private static final String PASSWORD_HASH = "encoded-password";
    private static final String PLAYER_ROLE = "PLAYER";

    private static final String NAME = "Juan";
    private static final String LAST_NAME = "Roa";
    private static final String PROGRAM = "SISTEMAS";
    private static final String IDENTIFICATION = "123456789";

    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 5, 15);

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
        RoleEntity roleEntity = createRoleEntity(ROLE_ID, PLAYER_ROLE);
        Role role = createRole(ROLE_ID, PLAYER_ROLE);

        AccountEntity entity = createValidEntity();
        entity.setRoles(List.of(roleEntity));

        when(roleMapper.toModel(roleEntity))
                .thenReturn(role);

        Account result = accountMapper.toModel(entity);

        assertNotNull(result);
        assertAccountFields(result);
        assertEquals(1, result.getRoles().size());
        assertEquals(PLAYER_ROLE, result.getRoles().get(0).getName());

        verify(roleMapper).toModel(roleEntity);
    }

    @Test
    void toModel_ShouldThrowException_WhenEntityHasNullRoles() {
        AccountEntity entity = createValidEntity();
        entity.setRoles(null);

        assertThrows(
                InvalidAccountBuildException.class,
                () -> accountMapper.toModel(entity)
        );

        verifyNoInteractions(roleMapper);
    }

    @Test
    void toEntity_ShouldReturnNull_WhenModelIsNull() {
        assertNull(accountMapper.toEntity(null));
    }

    @Test
    void toEntity_ShouldMapAllFields_WhenModelIsValid() {
        LocalDateTime now = LocalDateTime.now();

        Role role = createRole(ROLE_ID, PLAYER_ROLE);
        RoleEntity roleEntity = createRoleEntity(ROLE_ID, PLAYER_ROLE);

        Account model = createValidModel(now, List.of(role));

        when(roleMapper.toEntity(role))
                .thenReturn(roleEntity);

        AccountEntity result = accountMapper.toEntity(model);

        assertNotNull(result);
        assertEntityFields(result, now);
        assertEquals(1, result.getRoles().size());
        assertEquals(PLAYER_ROLE, result.getRoles().get(0).getName());

        verify(roleMapper).toEntity(role);
    }

    @Test
    void toResponse_ShouldReturnNull_WhenModelIsNull() {
        assertNull(accountMapper.toResponse(null));
    }

    @Test
    void toResponse_ShouldMapAllFields_WhenModelIsValid() {
        LocalDateTime now = LocalDateTime.now();

        Role role = createRole(ROLE_ID, PLAYER_ROLE);
        Account model = createValidModel(now, List.of(role));

        AccountResponse result = accountMapper.toResponse(model);

        assertNotNull(result);
        assertResponseFields(result, now);
        assertEquals(1, result.getRoles().size());
        assertEquals(PLAYER_ROLE, result.getRoles().get(0));
    }

    private AccountEntity createValidEntity() {
        AccountEntity entity = validAccountEntity(EMAIL);

        entity.setId(ACCOUNT_ID);
        entity.setName(NAME);
        entity.setLastName(LAST_NAME);
        entity.setBirthDate(BIRTH_DATE);
        entity.setRelation(Relation.ESTUDIANTE);
        entity.setSemester(7);
        entity.setProgram(PROGRAM);
        entity.setEmail(EMAIL);
        entity.setPasswordHash(PASSWORD_HASH);
        entity.setStatus(AccountStatus.ACTIVE);
        entity.setGender(Gender.MALE);
        entity.setIdentificationType(IdentificationType.CC);
        entity.setIdentification(IDENTIFICATION);

        return entity;
    }

    private Account createValidModel(LocalDateTime now, List<Role> roles) {
        return validAccountBuilder(EMAIL)
                .id(ACCOUNT_ID)
                .name(NAME)
                .lastName(LAST_NAME)
                .birthDate(BIRTH_DATE)
                .relation(Relation.ESTUDIANTE)
                .semester(7)
                .program(PROGRAM)
                .passwordHash(PASSWORD_HASH)
                .status(AccountStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .lastLoginAt(now)
                .gender(Gender.MALE)
                .identificationType(IdentificationType.CC)
                .identification(IDENTIFICATION)
                .roles(roles)
                .build();
    }

    private Role createRole(Long id, String roleName) {
        Role role = new Role();
        role.setId(id);
        role.setName(roleName);
        return role;
    }

    private RoleEntity createRoleEntity(Long id, String roleName) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(id);
        roleEntity.setName(roleName);
        return roleEntity;
    }

    private void assertAccountFields(Account result) {
        assertEquals(ACCOUNT_ID, result.getId());
        assertEquals(NAME, result.getName());
        assertEquals(LAST_NAME, result.getLastName());
        assertEquals(BIRTH_DATE, result.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, result.getRelation());
        assertEquals(7, result.getSemester());
        assertEquals(PROGRAM, result.getProgram());

        assertEquals(EMAIL, result.getEmail());
        assertEquals(PASSWORD_HASH, result.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());

        assertEquals(Gender.MALE, result.getGender());
        assertEquals(IdentificationType.CC, result.getIdentificationType());
        assertEquals(IDENTIFICATION, result.getIdentification());
    }

    private void assertEntityFields(AccountEntity result, LocalDateTime now) {
        assertEquals(ACCOUNT_ID, result.getId());
        assertEquals(NAME, result.getName());
        assertEquals(LAST_NAME, result.getLastName());
        assertEquals(BIRTH_DATE, result.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, result.getRelation());
        assertEquals(7, result.getSemester());
        assertEquals(PROGRAM, result.getProgram());

        assertEquals(EMAIL, result.getEmail());
        assertEquals(PASSWORD_HASH, result.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());

        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
        assertEquals(now, result.getLastLoginAt());

        assertEquals(Gender.MALE, result.getGender());
        assertEquals(IdentificationType.CC, result.getIdentificationType());
        assertEquals(IDENTIFICATION, result.getIdentification());
    }
    private void assertResponseFields(AccountResponse result, LocalDateTime now) {
        assertEquals(ACCOUNT_ID, result.getId());
        assertEquals(EMAIL, result.getEmail());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(now, result.getCreatedAt());

        assertEquals(NAME, result.getName());
        assertEquals(LAST_NAME, result.getLastName());
        assertEquals(BIRTH_DATE, result.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, result.getRelation());
        assertEquals(7, result.getSemester());
        assertEquals(PROGRAM, result.getProgram());

        assertEquals(Gender.MALE, result.getGender());
        assertEquals(IdentificationType.CC, result.getIdentificationType());
        assertEquals(IDENTIFICATION, result.getIdentification());
    }
}
