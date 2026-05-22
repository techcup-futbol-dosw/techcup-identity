package edu.eci.dosw.unitary.config;

import edu.eci.dosw.config.AdminDataInitializer;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.exception.RoleNotFoundException;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.model.*;
import edu.eci.dosw.repository.AccountRepository;
import edu.eci.dosw.repository.PermissionRepository;
import edu.eci.dosw.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDataInitializerTest {

    private static final String ADMIN_EMAIL = "admin@escuelaing.edu.co";
    private static final String ADMIN_PASSWORD = "Admin123*";
    private static final String ENCODED_PASSWORD = "encoded-admin-password";

    private static final String ADMIN_NAME = "Admin";
    private static final String ADMIN_LAST_NAME = "System";
    private static final String ADMIN_BIRTH_DATE = "2000-01-01";
    private static final Relation ADMIN_RELATION = Relation.ESTUDIANTE;
    private static final Integer ADMIN_SEMESTER = 1;
    private static final Program ADMIN_PROGRAM = Program.SISTEMAS;
    private static final Gender ADMIN_GENDER = Gender.MALE;
    private static final IdentificationType ADMIN_IDENTIFICATION_TYPE = IdentificationType.CC;
    private static final String ADMIN_IDENTIFICATION = "ADMIN-0001";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminDataInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new AdminDataInitializer();
    }

    @Test
    void initAdminAccount_ShouldSkip_WhenAdminAlreadyExists() throws Exception {
        AccountEntity existingAdmin = new AccountEntity();
        existingAdmin.setId(1L);

        when(accountRepository.findByEmail(ADMIN_EMAIL))
                .thenReturn(Optional.of(existingAdmin));

        CommandLineRunner runner = initializer.initAdminAccount(
                accountRepository,
                roleRepository,
                permissionRepository,
                accountMapper,
                passwordEncoder,
                ADMIN_EMAIL,
                ADMIN_PASSWORD,
                ADMIN_NAME,
                ADMIN_LAST_NAME,
                ADMIN_BIRTH_DATE,
                ADMIN_RELATION,
                ADMIN_SEMESTER,
                ADMIN_PROGRAM,
                ADMIN_GENDER,
                ADMIN_IDENTIFICATION_TYPE,
                ADMIN_IDENTIFICATION
        );

        runner.run();

        verify(accountRepository).findByEmail(ADMIN_EMAIL);
        verifyNoInteractions(roleRepository, permissionRepository, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void initAdminAccount_ShouldThrowException_WhenAdminRoleDoesNotExist() {
        when(accountRepository.findByEmail(ADMIN_EMAIL))
                .thenReturn(Optional.empty());

        when(roleRepository.findByNameIgnoreCase("ADMIN"))
                .thenReturn(Optional.empty());

        CommandLineRunner runner = initializer.initAdminAccount(
                accountRepository,
                roleRepository,
                permissionRepository,
                accountMapper,
                passwordEncoder,
                ADMIN_EMAIL,
                ADMIN_PASSWORD,
                ADMIN_NAME,
                ADMIN_LAST_NAME,
                ADMIN_BIRTH_DATE,
                ADMIN_RELATION,
                ADMIN_SEMESTER,
                ADMIN_PROGRAM,
                ADMIN_GENDER,
                ADMIN_IDENTIFICATION_TYPE,
                ADMIN_IDENTIFICATION
        );

        RoleNotFoundException ex = assertThrows(
                RoleNotFoundException.class,
                runner::run
        );

        assertEquals("Role not found: ADMIN", ex.getMessage());

        verify(accountRepository).findByEmail(ADMIN_EMAIL);
        verify(roleRepository).findByNameIgnoreCase("ADMIN");
        verify(accountRepository, never()).save(any());
        verify(accountMapper, never()).toEntity(any());
    }

    @Test
    void initAdminAccount_ShouldCreateAdminAccount_WhenAdminDoesNotExist() throws Exception {
        RoleEntity adminRoleEntity = roleEntity(10L, "ADMIN");
        List<PermissionEntity> permissions = List.of(
                permission("account:read:any"),
                permission("role:assign:any")
        );

        AccountEntity mappedAccountEntity = new AccountEntity();
        mappedAccountEntity.setId(99L);

        when(accountRepository.findByEmail(ADMIN_EMAIL))
                .thenReturn(Optional.empty());

        when(roleRepository.findByNameIgnoreCase("ADMIN"))
                .thenReturn(Optional.of(adminRoleEntity));

        when(permissionRepository.findAll())
                .thenReturn(permissions);

        when(passwordEncoder.encode(ADMIN_PASSWORD))
                .thenReturn(ENCODED_PASSWORD);

        when(accountMapper.toEntity(any(Account.class)))
                .thenReturn(mappedAccountEntity);

        when(accountRepository.save(mappedAccountEntity))
                .thenReturn(mappedAccountEntity);

        CommandLineRunner runner = initializer.initAdminAccount(
                accountRepository,
                roleRepository,
                permissionRepository,
                accountMapper,
                passwordEncoder,
                ADMIN_EMAIL,
                ADMIN_PASSWORD,
                ADMIN_NAME,
                ADMIN_LAST_NAME,
                ADMIN_BIRTH_DATE,
                ADMIN_RELATION,
                ADMIN_SEMESTER,
                ADMIN_PROGRAM,
                ADMIN_GENDER,
                ADMIN_IDENTIFICATION_TYPE,
                ADMIN_IDENTIFICATION
        );

        runner.run();

        verify(accountRepository).findByEmail(ADMIN_EMAIL);
        verify(roleRepository).findByNameIgnoreCase("ADMIN");
        verify(permissionRepository).findAll();
        verify(passwordEncoder).encode(ADMIN_PASSWORD);
        verify(roleRepository).save(adminRoleEntity);
        verify(accountMapper).toEntity(any(Account.class));
        verify(accountRepository).save(mappedAccountEntity);

        assertEquals(permissions, adminRoleEntity.getPermissions());

        Account builtAdmin = capturedMappedAccount();
        assertBuiltAdminFields(builtAdmin);
        assertEquals(1, builtAdmin.getRoles().size());
        assertEquals("ADMIN", builtAdmin.getRoles().get(0).getName());
        assertEquals(10L, builtAdmin.getRoles().get(0).getId());
    }

    private Account capturedMappedAccount() {
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).toEntity(captor.capture());
        return captor.getValue();
    }

    private void assertBuiltAdminFields(Account account) {
        assertEquals(ADMIN_NAME, account.getName());
        assertEquals(ADMIN_LAST_NAME, account.getLastName());
        assertEquals(LocalDate.parse(ADMIN_BIRTH_DATE), account.getBirthDate());
        assertEquals(ADMIN_RELATION, account.getRelation());
        assertEquals(ADMIN_SEMESTER, account.getSemester());
        assertEquals(ADMIN_PROGRAM, account.getProgram());

        assertEquals(ADMIN_EMAIL, account.getEmail());
        assertEquals(ENCODED_PASSWORD, account.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());

        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());

        assertEquals(ADMIN_GENDER, account.getGender());
        assertEquals(ADMIN_IDENTIFICATION_TYPE, account.getIdentificationType());
        assertEquals(ADMIN_IDENTIFICATION, account.getIdentification());
    }

    private RoleEntity roleEntity(Long id, String name) {
        RoleEntity role = new RoleEntity();
        role.setId(id);
        role.setName(name);
        role.setPermissions(new ArrayList<>());
        return role;
    }

    private PermissionEntity permission(String name) {
        PermissionEntity permission = new PermissionEntity();
        permission.setName(name);
        return permission;
    }
}
