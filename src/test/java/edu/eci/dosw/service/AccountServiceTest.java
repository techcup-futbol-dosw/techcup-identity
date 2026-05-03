package edu.eci.dosw.service;

import edu.eci.dosw.dto.RegisterAccountRequest;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.model.Role;
import edu.eci.dosw.repository.AccountRepository;
import edu.eci.dosw.repository.RoleRepository;
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
        // Arrange
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");
        request.setRelation("student");

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName("PLAYER");

        Role playerRole = new Role();
        playerRole.setName("PLAYER");

        AccountEntity entityToSave = new AccountEntity();
        AccountEntity savedEntity = new AccountEntity();
        savedEntity.setId(1L);
        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("123456")
                .addRole(new Role())
                .createdAt(LocalDateTime.now());

        Account savedModel = accountBuilder.build();

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.empty());

        when(roleRepository.findByName("PLAYER"))
                .thenReturn(Optional.of(roleEntity));

        when(roleMapper.toModel(roleEntity))
                .thenReturn(playerRole);

        when(accountMapper.toEntity(any(Account.class)))
                .thenReturn(entityToSave);

        when(accountRepository.save(entityToSave))
                .thenReturn(savedEntity);

        when(accountMapper.toModel(savedEntity))
                .thenReturn(savedModel);
        when(passwordEncoder.encode("123456"))
                .thenReturn("encoded-password");

        // Act
        Account result = accountService.register(request);

        // Assert
        assertNotNull(result);
        assertEquals("juan@escuelaing.edu.co", result.getEmail());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verify(roleRepository).findByName("PLAYER");
        verify(roleMapper).toModel(roleEntity);
        verify(accountRepository).save(entityToSave);
        verify(accountMapper).toModel(savedEntity);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).toEntity(accountCaptor.capture());

        Account builtAccount = accountCaptor.getValue();
        assertEquals("juan@escuelaing.edu.co", builtAccount.getEmail());
        assertEquals("encoded-password", builtAccount.getPassword());
        assertNotNull(builtAccount.getCreatedAt());

    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");
        request.setRelation("student");

        AccountEntity existing = new AccountEntity();
        existing.setId(99L);

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.of(existing));

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.register(request));

        // Assert
        assertEquals("Email already registered", ex.getMessage());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenFamilyEmailIsNotGmail() {
        // Arrange
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("persona@yahoo.com");
        request.setPassword("123456");
        request.setRelation("family");

        when(accountRepository.findByEmail("persona@yahoo.com"))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.register(request));

        // Assert
        assertEquals("Family accounts must use a Gmail address", ex.getMessage());

        verify(accountRepository).findByEmail("persona@yahoo.com");
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenInstitutionalRelationHasNonInstitutionalEmail() {
        // Arrange
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("juan@gmail.com");
        request.setPassword("123456");
        request.setRelation("student");

        when(accountRepository.findByEmail("juan@gmail.com"))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.register(request));

        // Assert
        assertEquals("Institutional relation requires institutional email", ex.getMessage());

        verify(accountRepository).findByEmail("juan@gmail.com");
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenEmailOrRelationIsNull() {
        // Arrange
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail(null);
        request.setPassword("123456");
        request.setRelation(null);

        when(accountRepository.findByEmail(null))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.register(request));

        // Assert
        assertEquals("Invalid registration data", ex.getMessage());

        verify(accountRepository).findByEmail(null);
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenRoleDoesNotExist() {
        // Arrange
        RegisterAccountRequest request = new RegisterAccountRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");
        request.setRelation("student");

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.empty());

        when(roleRepository.findByName("PLAYER"))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.register(request));

        // Assert
        assertEquals("Role not found", ex.getMessage());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verify(roleRepository).findByName("PLAYER");
        verifyNoInteractions(roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void findById_ShouldReturnAccount_WhenAccountExists() {
        // Arrange
        Long accountId = 1L;

        AccountEntity entity = new AccountEntity();
        entity.setId(accountId);

        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("123456")
                .createdAt(LocalDateTime.now())
                .addRole(new Role());

        Account model = accountBuilder.build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(entity));

        when(accountMapper.toModel(entity))
                .thenReturn(model);

        // Act
        Account result = accountService.findById(accountId);

        // Assert
        assertNotNull(result);
        assertEquals("juan@escuelaing.edu.co", result.getEmail());

        verify(accountRepository).findById(accountId);
        verify(accountMapper).toModel(entity);
    }

    @Test
    void findById_ShouldThrowException_WhenAccountDoesNotExist() {
        // Arrange
        Long accountId = 999L;

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.findById(accountId));

        // Assert
        assertEquals("Account not found", ex.getMessage());

        verify(accountRepository).findById(accountId);
        verify(accountMapper, never()).toModel(any());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Arrange
        String email = "juan@escuelaing.edu.co";

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.of(new AccountEntity()));

        // Act
        boolean result = accountService.existsByEmail(email);

        // Assert
        assertTrue(result);
        verify(accountRepository).findByEmail(email);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // Arrange
        String email = "juan@escuelaing.edu.co";

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act
        boolean result = accountService.existsByEmail(email);

        // Assert
        assertFalse(result);
        verify(accountRepository).findByEmail(email);
    }
}