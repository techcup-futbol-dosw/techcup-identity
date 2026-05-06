package edu.eci.dosw.unitaria.service;

import edu.eci.dosw.dto.AccountResponse;
import edu.eci.dosw.dto.RegisterAccountRequest;
import edu.eci.dosw.dto.Relation;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.model.Role;
import edu.eci.dosw.repository.AccountRepository;
import edu.eci.dosw.repository.RoleRepository;
import edu.eci.dosw.service.AccountService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    @Test
    void register_ShouldCreateAccount_WhenRequestIsValid() {
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");
        request.setRelation(Relation.ESTUDIANTE);
        request.setSemester(3); // ESTUDIANTE requiere semester

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName("PLAYER");

        Role playerRole = new Role();
        playerRole.setName("PLAYER");

        AccountEntity savedEntity = new AccountEntity();
        savedEntity.setId(1L);

        Account savedModel = new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .addRole(playerRole)
                .createdAt(LocalDateTime.now())
                .build();

        AccountResponse expectedResponse = new AccountResponse();
        expectedResponse.setEmail("juan@escuelaing.edu.co");

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.empty());
        when(roleRepository.findByNameIgnoreCase("PLAYER"))
                .thenReturn(Optional.of(roleEntity));
        when(roleMapper.toModel(roleEntity))
                .thenReturn(playerRole);
        when(passwordEncoder.encode("123456"))
                .thenReturn("encoded-password");
        when(accountMapper.toEntity(any(Account.class)))
                .thenReturn(savedEntity);
        when(accountRepository.save(savedEntity))
                .thenReturn(savedEntity);
        when(accountMapper.toModel(savedEntity))
                .thenReturn(savedModel);
        when(accountMapper.toResponse(savedModel))
                .thenReturn(expectedResponse);

        AccountResponse result = accountService.register(request);

        assertNotNull(result);
        assertEquals("juan@escuelaing.edu.co", result.getEmail());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verify(roleRepository).findByNameIgnoreCase("PLAYER");
        verify(roleMapper).toModel(roleEntity);
        verify(passwordEncoder).encode("123456");
        verify(accountRepository).save(any(AccountEntity.class));
        verify(accountMapper).toModel(savedEntity);
        verify(accountMapper).toResponse(savedModel);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).toEntity(accountCaptor.capture());
        Account builtAccount = accountCaptor.getValue();
        assertEquals("juan@escuelaing.edu.co", builtAccount.getEmail());
        assertEquals("encoded-password", builtAccount.getPassword());
        assertNotNull(builtAccount.getCreatedAt());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");
        request.setRelation(Relation.ESTUDIANTE);

        AccountEntity existing = new AccountEntity();
        existing.setId(99L);

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.register(request));

        assertEquals("Email already registered", ex.getMessage());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenFamilyEmailIsNotGmail() {
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("persona@yahoo.com");
        request.setPassword("123456");
        request.setRelation(Relation.FAMILIAR);

        when(accountRepository.findByEmail("persona@yahoo.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.register(request));

        assertEquals("Family accounts must use a Gmail address", ex.getMessage());

        verify(accountRepository).findByEmail("persona@yahoo.com");
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenInstitutionalRelationHasNonInstitutionalEmail() {
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("juan@gmail.com");
        request.setPassword("123456");
        request.setRelation(Relation.ESTUDIANTE);

        when(accountRepository.findByEmail("juan@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.register(request));

        assertEquals("Institutional relation requires institutional email", ex.getMessage());

        verify(accountRepository).findByEmail("juan@gmail.com");
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenEmailOrRelationIsNull() {
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");
        request.setRelation(null); // solo relation null es suficiente para disparar la validación

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.register(request));

        assertEquals("Invalid registration data", ex.getMessage());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenRoleDoesNotExist() {
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");
        request.setRelation(Relation.ESTUDIANTE);
        request.setSemester(3);

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.empty());
        when(roleRepository.findByNameIgnoreCase("PLAYER"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.register(request));

        assertEquals("Role not found", ex.getMessage());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verify(roleRepository).findByNameIgnoreCase("PLAYER");
        verifyNoInteractions(roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void findById_ShouldReturnAccount_WhenAccountExists() {
        Long accountId = 1L;

        AccountEntity entity = new AccountEntity();
        entity.setId(accountId);

        Account model = new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .addRole(new Role())
                .build();

        AccountResponse expectedResponse = new AccountResponse();
        expectedResponse.setEmail("juan@escuelaing.edu.co");

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(entity));
        when(accountMapper.toModel(entity))
                .thenReturn(model);
        when(accountMapper.toResponse(model))
                .thenReturn(expectedResponse);

        AccountResponse result = accountService.findById(accountId);

        assertNotNull(result);
        assertEquals("juan@escuelaing.edu.co", result.getEmail());

        verify(accountRepository).findById(accountId);
        verify(accountMapper).toModel(entity);
        verify(accountMapper).toResponse(model);
    }

    @Test
    void findById_ShouldThrowException_WhenAccountDoesNotExist() {
        Long accountId = 999L;

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.findById(accountId));

        assertEquals("Account not found", ex.getMessage());

        verify(accountRepository).findById(accountId);
        verify(accountMapper, never()).toModel(any());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        String email = "juan@escuelaing.edu.co";

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.of(new AccountEntity()));

        boolean result = accountService.existsByEmail(email);

        assertTrue(result);
        verify(accountRepository).findByEmail(email);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        String email = "juan@escuelaing.edu.co";

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        boolean result = accountService.existsByEmail(email);

        assertFalse(result);
        verify(accountRepository).findByEmail(email);
    }
}