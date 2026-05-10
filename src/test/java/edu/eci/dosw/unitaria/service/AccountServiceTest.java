package edu.eci.dosw.unitaria.service;

import edu.eci.dosw.dto.AccountResponse;
import edu.eci.dosw.model.Program;
import edu.eci.dosw.dto.RegisterAccountRequest;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.exception.AccountNotFoundException;
import edu.eci.dosw.exception.EmailAlreadyRegisteredException;
import edu.eci.dosw.exception.InvalidEmailForRelationException;
import edu.eci.dosw.exception.InvalidRegistrationDataException;
import edu.eci.dosw.exception.RoleNotFoundException;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.model.Gender;
import edu.eci.dosw.model.IdentificationType;
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

import java.time.LocalDate;
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
        RegisterAccountRequest request = createValidRegisterRequest("juan@mail.escuelaing.edu.co");
        request.setPassword("Password123*");

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName("PLAYER");

        Role playerRole = new Role();
        playerRole.setId(1L);
        playerRole.setName("PLAYER");

        AccountEntity savedEntity = new AccountEntity();
        savedEntity.setId(1L);

        Account savedModel = createValidAccount("juan@mail.escuelaing.edu.co", "encoded-password", playerRole);

        AccountResponse expectedResponse = new AccountResponse();
        expectedResponse.setEmail("juan@mail.escuelaing.edu.co");

        when(accountRepository.findByEmail("juan@mail.escuelaing.edu.co"))
                .thenReturn(Optional.empty());
        when(roleRepository.findByNameIgnoreCase("PLAYER"))
                .thenReturn(Optional.of(roleEntity));
        when(roleMapper.toModel(roleEntity))
                .thenReturn(playerRole);
        when(passwordEncoder.encode("Password123*"))
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
        assertEquals("juan@mail.escuelaing.edu.co", result.getEmail());

        verify(accountRepository).findByEmail("juan@mail.escuelaing.edu.co");
        verify(roleRepository).findByNameIgnoreCase("PLAYER");
        verify(roleMapper).toModel(roleEntity);
        verify(passwordEncoder).encode("Password123*");
        verify(accountRepository).save(any(AccountEntity.class));
        verify(accountMapper).toModel(savedEntity);
        verify(accountMapper).toResponse(savedModel);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).toEntity(accountCaptor.capture());

        Account builtAccount = accountCaptor.getValue();

        assertEquals("Juan", builtAccount.getName());
        assertEquals("Roa", builtAccount.getLastName());
        assertEquals(LocalDate.of(2000, 5, 15), builtAccount.getBirthDate());
        assertEquals(Relation.ESTUDIANTE, builtAccount.getRelation());
        assertEquals(3, builtAccount.getSemester());
        assertEquals("SISTEMAS", builtAccount.getProgram());

        assertEquals("juan@mail.escuelaing.edu.co", builtAccount.getEmail());
        assertEquals("encoded-password", builtAccount.getPasswordHash());

        assertEquals(Gender.MALE, builtAccount.getGender());
        assertEquals(IdentificationType.CC, builtAccount.getIdentificationType());
        assertEquals("123456789", builtAccount.getIdentification());

        assertNotNull(builtAccount.getCreatedAt());
        assertEquals(1, builtAccount.getRoles().size());
        assertEquals("PLAYER", builtAccount.getRoles().get(0).getName());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        RegisterAccountRequest request = createValidRegisterRequest("juan@mail.escuelaing.edu.co");

        AccountEntity existing = new AccountEntity();
        existing.setId(99L);

        when(accountRepository.findByEmail("juan@mail.escuelaing.edu.co"))
                .thenReturn(Optional.of(existing));

        EmailAlreadyRegisteredException ex = assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> accountService.register(request)
        );

        assertEquals("Email already registered: juan@mail.escuelaing.edu.co", ex.getMessage());

        verify(accountRepository).findByEmail("juan@mail.escuelaing.edu.co");
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenFamilyEmailIsNotGmail() {
        RegisterAccountRequest request = createValidRegisterRequest("persona@yahoo.com");
        request.setRelation(Relation.FAMILIAR);

        when(accountRepository.findByEmail("persona@yahoo.com"))
                .thenReturn(Optional.empty());

        InvalidEmailForRelationException ex = assertThrows(
                InvalidEmailForRelationException.class,
                () -> accountService.register(request)
        );

        assertEquals("Family accounts must use a Gmail address", ex.getMessage());

        verify(accountRepository).findByEmail("persona@yahoo.com");
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenInstitutionalRelationHasNonInstitutionalEmail() {
        RegisterAccountRequest request = createValidRegisterRequest("juan@gmail.com");
        request.setRelation(Relation.ESTUDIANTE);

        when(accountRepository.findByEmail("juan@gmail.com"))
                .thenReturn(Optional.empty());

        InvalidEmailForRelationException ex = assertThrows(
                InvalidEmailForRelationException.class,
                () -> accountService.register(request)
        );

        assertEquals("Institutional relation requires institutional email", ex.getMessage());

        verify(accountRepository).findByEmail("juan@gmail.com");
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenEmailOrRelationIsNull() {
        RegisterAccountRequest request = createValidRegisterRequest("juan@mail.escuelaing.edu.co");
        request.setRelation(null);

        when(accountRepository.findByEmail("juan@mail.escuelaing.edu.co"))
                .thenReturn(Optional.empty());

        InvalidRegistrationDataException ex = assertThrows(
                InvalidRegistrationDataException.class,
                () -> accountService.register(request)
        );

        assertEquals("Invalid registration data", ex.getMessage());

        verify(accountRepository).findByEmail("juan@mail.escuelaing.edu.co");
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenRoleDoesNotExist() {
        RegisterAccountRequest request = createValidRegisterRequest("juan@mail.escuelaing.edu.co");

        when(accountRepository.findByEmail("juan@mail.escuelaing.edu.co"))
                .thenReturn(Optional.empty());
        when(roleRepository.findByNameIgnoreCase("PLAYER"))
                .thenReturn(Optional.empty());

        RoleNotFoundException ex = assertThrows(
                RoleNotFoundException.class,
                () -> accountService.register(request)
        );

        assertEquals("Role not found: PLAYER", ex.getMessage());

        verify(accountRepository).findByEmail("juan@mail.escuelaing.edu.co");
        verify(roleRepository).findByNameIgnoreCase("PLAYER");
        verifyNoInteractions(roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void findById_ShouldReturnAccount_WhenAccountExists() {
        Long accountId = 1L;

        AccountEntity entity = new AccountEntity();
        entity.setId(accountId);

        Role playerRole = new Role();
        playerRole.setId(1L);
        playerRole.setName("PLAYER");

        Account model = createValidAccount("juan@mail.escuelaing.edu.co", "encoded-password", playerRole);

        AccountResponse expectedResponse = new AccountResponse();
        expectedResponse.setEmail("juan@mail.escuelaing.edu.co");

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(entity));
        when(accountMapper.toModel(entity))
                .thenReturn(model);
        when(accountMapper.toResponse(model))
                .thenReturn(expectedResponse);

        AccountResponse result = accountService.findById(accountId);

        assertNotNull(result);
        assertEquals("juan@mail.escuelaing.edu.co", result.getEmail());

        verify(accountRepository).findById(accountId);
        verify(accountMapper).toModel(entity);
        verify(accountMapper).toResponse(model);
    }

    @Test
    void findById_ShouldThrowException_WhenAccountDoesNotExist() {
        Long accountId = 999L;

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        AccountNotFoundException ex = assertThrows(
                AccountNotFoundException.class,
                () -> accountService.findById(accountId)
        );

        assertEquals("Account not found with id: 999", ex.getMessage());

        verify(accountRepository).findById(accountId);
        verify(accountMapper, never()).toModel(any());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        String email = "juan@mail.escuelaing.edu.co";

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.of(new AccountEntity()));

        boolean result = accountService.existsByEmail(email);

        assertTrue(result);
        verify(accountRepository).findByEmail(email);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        String email = "juan@mail.escuelaing.edu.co";

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        boolean result = accountService.existsByEmail(email);

        assertFalse(result);
        verify(accountRepository).findByEmail(email);
    }

    private RegisterAccountRequest createValidRegisterRequest(String email) {
        RegisterAccountRequest request = new RegisterAccountRequest();

        request.setEmail(email);
        request.setPassword("Password123*");
        request.setRelation(Relation.ESTUDIANTE);
        request.setProgram(Program.SISTEMAS);
        request.setSemester(3);

        request.setName("Juan");
        request.setLastName("Roa");
        request.setBirthDate(LocalDate.of(2000, 5, 15));
        request.setGender(Gender.MALE);
        request.setIdentificationType(IdentificationType.CC);
        request.setIdentification("123456789");

        return request;
    }

    private Account createValidAccount(String email, String passwordHash, Role role) {
        return new AccountBuilder()
                .id(1L)
                .name("Juan")
                .lastName("Roa")
                .birthDate(LocalDate.of(2000, 5, 15))
                .relation(Relation.ESTUDIANTE)
                .semester(3)
                .program("SISTEMAS")
                .email(email)
                .passwordHash(passwordHash)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .gender(Gender.MALE)
                .identificationType(IdentificationType.CC)
                .identification("123456789")
                .addRole(role)
                .build();
    }
}