package edu.eci.dosw.unitary.service;
import edu.eci.dosw.dto.*;
import edu.eci.dosw.exception.*;
import edu.eci.dosw.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static edu.eci.dosw.testutil.TestDataFactory.validAccountBuilder;
import static edu.eci.dosw.testutil.TestDataFactory.validRegisterRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.repository.AccountRepository;
import edu.eci.dosw.repository.RoleRepository;
import edu.eci.dosw.service.AccountService;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long MISSING_ACCOUNT_ID = 999L;

    private static final String EMAIL = "juan@mail.escuelaing.edu.co";
    private static final String FAMILY_INVALID_EMAIL = "persona@yahoo.com";
    private static final String INSTITUTIONAL_INVALID_EMAIL = "juan@gmail.com";

    private static final String PASSWORD = "Password123*";
    private static final String ENCODED_PASSWORD = "encoded-password";

    private static final String PLAYER_ROLE = "PLAYER";

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
        RegisterAccountRequest request = validRegisterRequest(EMAIL);
        request.setPassword(PASSWORD);

        RoleEntity roleEntity = createRoleEntity(PLAYER_ROLE);
        Role playerRole = createRole(ACCOUNT_ID, PLAYER_ROLE);

        AccountEntity savedEntity = createAccountEntity(ACCOUNT_ID);
        Account savedModel = createAccountModel(EMAIL, ENCODED_PASSWORD, playerRole);

        AccountResponse expectedResponse = createAccountResponse(EMAIL);

        when(accountRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        when(roleRepository.findByNameIgnoreCase(PLAYER_ROLE))
                .thenReturn(Optional.of(roleEntity));

        when(roleMapper.toModel(roleEntity))
                .thenReturn(playerRole);

        when(passwordEncoder.encode(PASSWORD))
                .thenReturn(ENCODED_PASSWORD);

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
        assertEquals(EMAIL, result.getEmail());

        verify(accountRepository).findByEmail(EMAIL);
        verify(roleRepository).findByNameIgnoreCase(PLAYER_ROLE);
        verify(roleMapper).toModel(roleEntity);
        verify(passwordEncoder).encode(PASSWORD);
        verify(accountRepository).save(any(AccountEntity.class));
        verify(accountMapper).toModel(savedEntity);
        verify(accountMapper).toResponse(savedModel);

        Account builtAccount = captureBuiltAccount();

        assertEquals(request.getName(), builtAccount.getName());
        assertEquals(request.getLastName(), builtAccount.getLastName());
        assertEquals(request.getBirthDate(), builtAccount.getBirthDate());
        assertEquals(request.getRelation(), builtAccount.getRelation());
        assertEquals(request.getSemester(), builtAccount.getSemester());
        assertEquals(request.getProgram().name(), builtAccount.getProgram());

        assertEquals(request.getEmail(), builtAccount.getEmail());
        assertEquals(ENCODED_PASSWORD, builtAccount.getPasswordHash());

        assertEquals(request.getGender(), builtAccount.getGender());
        assertEquals(request.getIdentificationType(), builtAccount.getIdentificationType());
        assertEquals(request.getIdentification(), builtAccount.getIdentification());

        assertNotNull(builtAccount.getCreatedAt());
        assertEquals(1, builtAccount.getRoles().size());
        assertEquals(PLAYER_ROLE, builtAccount.getRoles().get(0).getName());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        RegisterAccountRequest request = validRegisterRequest(EMAIL);

        AccountEntity existing = createAccountEntity(99L);

        when(accountRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(existing));

        EmailAlreadyRegisteredException ex = assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> accountService.register(request)
        );

        assertEquals("Email already registered: " + EMAIL, ex.getMessage());

        verify(accountRepository).findByEmail(EMAIL);
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenStudentHasNoSemester() {
        RegisterAccountRequest request = validRegisterRequest(EMAIL);
        request.setRelation(Relation.ESTUDIANTE);
        request.setSemester(null);

        when(accountRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        MissingRequiredFieldException ex = assertThrows(
                MissingRequiredFieldException.class,
                () -> accountService.register(request)
        );

        assertEquals("Semester is required", ex.getMessage());

        verify(accountRepository).findByEmail(EMAIL);
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenFamilyEmailIsNotGmail() {
        RegisterAccountRequest request = validRegisterRequest(FAMILY_INVALID_EMAIL);
        request.setRelation(Relation.FAMILIAR);

        when(accountRepository.findByEmail(FAMILY_INVALID_EMAIL))
                .thenReturn(Optional.empty());

        InvalidEmailForRelationException ex = assertThrows(
                InvalidEmailForRelationException.class,
                () -> accountService.register(request)
        );

        assertEquals("Family accounts must use a Gmail address", ex.getMessage());

        verify(accountRepository).findByEmail(FAMILY_INVALID_EMAIL);
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenInstitutionalRelationHasNonInstitutionalEmail() {
        RegisterAccountRequest request = validRegisterRequest(INSTITUTIONAL_INVALID_EMAIL);
        request.setRelation(Relation.ESTUDIANTE);

        when(accountRepository.findByEmail(INSTITUTIONAL_INVALID_EMAIL))
                .thenReturn(Optional.empty());

        InvalidEmailForRelationException ex = assertThrows(
                InvalidEmailForRelationException.class,
                () -> accountService.register(request)
        );

        assertEquals("Institutional relation requires institutional email", ex.getMessage());

        verify(accountRepository).findByEmail(INSTITUTIONAL_INVALID_EMAIL);
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenEmailOrRelationIsNull() {
        RegisterAccountRequest request = validRegisterRequest(EMAIL);
        request.setRelation(null);

        when(accountRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        InvalidRegistrationDataException ex = assertThrows(
                InvalidRegistrationDataException.class,
                () -> accountService.register(request)
        );

        assertEquals("Invalid registration data", ex.getMessage());

        verify(accountRepository).findByEmail(EMAIL);
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenRoleDoesNotExist() {
        RegisterAccountRequest request = validRegisterRequest(EMAIL);

        when(accountRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        when(roleRepository.findByNameIgnoreCase(PLAYER_ROLE))
                .thenReturn(Optional.empty());

        RoleNotFoundException ex = assertThrows(
                RoleNotFoundException.class,
                () -> accountService.register(request)
        );

        assertEquals("Role not found: PLAYER", ex.getMessage());

        verify(accountRepository).findByEmail(EMAIL);
        verify(roleRepository).findByNameIgnoreCase(PLAYER_ROLE);
        verifyNoInteractions(roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void findById_ShouldReturnAccount_WhenAccountExists() {
        AccountEntity entity = createAccountEntity(ACCOUNT_ID);

        Role playerRole = createRole(ACCOUNT_ID, PLAYER_ROLE);
        Account model = createAccountModel(EMAIL, ENCODED_PASSWORD, playerRole);

        AccountResponse expectedResponse = createAccountResponse(EMAIL);

        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(entity));

        when(accountMapper.toModel(entity))
                .thenReturn(model);

        when(accountMapper.toResponse(model))
                .thenReturn(expectedResponse);

        AccountResponse result = accountService.findById(ACCOUNT_ID);

        assertNotNull(result);
        assertEquals(EMAIL, result.getEmail());

        verify(accountRepository).findById(ACCOUNT_ID);
        verify(accountMapper).toModel(entity);
        verify(accountMapper).toResponse(model);
    }

    @Test
    void findById_ShouldThrowException_WhenAccountDoesNotExist() {
        when(accountRepository.findById(MISSING_ACCOUNT_ID))
                .thenReturn(Optional.empty());

        AccountNotFoundException ex = assertThrows(
                AccountNotFoundException.class,
                () -> accountService.findById(MISSING_ACCOUNT_ID)
        );

        assertEquals("Account not found with id: 999", ex.getMessage());

        verify(accountRepository).findById(MISSING_ACCOUNT_ID);
        verify(accountMapper, never()).toModel(any());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        when(accountRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(new AccountEntity()));

        boolean result = accountService.existsByEmail(EMAIL);

        assertTrue(result);
        verify(accountRepository).findByEmail(EMAIL);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        when(accountRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        boolean result = accountService.existsByEmail(EMAIL);

        assertFalse(result);
        verify(accountRepository).findByEmail(EMAIL);
    }

    @Test
    void searchAccounts_ShouldReturnMappedPage_WhenRepositoryReturnsResults() {
        AccountAdminSearchCriteria criteria = createAccountAdminSearchCriteria("juan", "PLAYER", AccountStatus.ACTIVE,0,10,"name,asc");
        AccountEntity entity = createAccountEntity(1L);
        Role role = createRole(5L,"PLAYER");
        Account model = createAccountModel("juan@escuelaing.edu.co","password123",role);

        AccountAdminItemResponse itemResponse = createAccountAdminItemResponse(1L,"Juan Perez", "juan@escuelaing.edu.co",List.of("PLAYER"));

        Page<AccountEntity> accountPage = new PageImpl<>(
                List.of(entity),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")),
                1
        );


        when(accountRepository.searchForAdmin(
                eq("juan"),
                eq("player"),
                eq(AccountStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(accountPage);

        when(accountMapper.toModel(entity)).thenReturn(model);
        when(accountMapper.toAdminItemResponse(model)).thenReturn(itemResponse);

        AccountAdminPageResponse result = accountService.searchAccounts(criteria);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        AccountAdminItemResponse row = result.getContent().get(0);
        assertEquals(1L, row.getId());
        assertEquals("Juan Perez", row.getFullName());
        assertEquals("juan@escuelaing.edu.co", row.getEmail());
        assertEquals(List.of("PLAYER"), row.getRoles());

        verify(accountRepository).searchForAdmin(
                eq("juan"),
                eq("player"),
                eq(AccountStatus.ACTIVE),
                any(Pageable.class)
        );
        verify(accountMapper).toModel(entity);
        verify(accountMapper).toAdminItemResponse(model);
    }

    @Test
    void searchAccounts_ShouldUseDefaultPagingAndSort_WhenCriteriaDoesNotProvideThem() {
        AccountAdminSearchCriteria criteria =
                createAccountAdminSearchCriteria(null, null, null, null, null, null);

        Page<AccountEntity> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")),
                0
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(accountRepository.searchForAdmin(
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        AccountAdminPageResponse result = accountService.searchAccounts(criteria);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());

        verify(accountRepository).searchForAdmin(
                isNull(),
                isNull(),
                isNull(),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "name"), pageable.getSort());
    }

    @Test
    void searchAccounts_ShouldNormalizeQueryAndRole_WhenFiltersAreProvided() {
        AccountAdminSearchCriteria criteria =
                createAccountAdminSearchCriteria("   JUAN   ", "   ADMIN   ", AccountStatus.ACTIVE, 1, 5, "email,desc");

        Page<AccountEntity> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "email")),
                0
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(accountRepository.searchForAdmin(
                eq("juan"),
                eq("admin"),
                eq(AccountStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        AccountAdminPageResponse result = accountService.searchAccounts(criteria);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(1, result.getPage());
        assertEquals(5, result.getSize());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());

        verify(accountRepository).searchForAdmin(
                eq("juan"),
                eq("admin"),
                eq(AccountStatus.ACTIVE),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "email"), pageable.getSort());
    }

    @Test
    void searchAccounts_ShouldReturnEmptyPage_WhenRepositoryReturnsNoResults() {
        AccountAdminSearchCriteria criteria =
                createAccountAdminSearchCriteria("nadie", null, null, 0, 10, "name,asc");

        Page<AccountEntity> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")),
                0
        );

        when(accountRepository.searchForAdmin(
                eq("nadie"),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        AccountAdminPageResponse result = accountService.searchAccounts(criteria);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());

        verify(accountRepository).searchForAdmin(
                eq("nadie"),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
        verify(accountMapper, never()).toModel(any());
        verify(accountMapper, never()).toAdminItemResponse(any());
    }

    @Test
    void register_ShouldThrowException_WhenIdentificationAlreadyExists() {
        RegisterAccountRequest request =
                validRegisterRequest("juan@mail.escuelaing.edu.co");

        request.setIdentificationType(IdentificationType.CC);
        request.setIdentification("123456789");

        when(accountRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(accountRepository.existsByIdentificationTypeAndIdentificationIgnoreCase(
                IdentificationType.CC,
                "123456789"
        )).thenReturn(true);

        IdentificationAlreadyRegisteredException ex = assertThrows(
                IdentificationAlreadyRegisteredException.class,
                () -> accountService.register(request)
        );

        assertEquals(
                "Identification already registered: CC 123456789",
                ex.getMessage()
        );

        verify(accountRepository).findByEmail(request.getEmail());
        verify(accountRepository).existsByIdentificationTypeAndIdentificationIgnoreCase(
                IdentificationType.CC,
                "123456789"
        );
        verifyNoInteractions(roleRepository, roleMapper, accountMapper, passwordEncoder);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void existsByIdentification_ShouldReturnTrue_WhenIdentificationExists() {
        when(accountRepository.existsByIdentificationTypeAndIdentificationIgnoreCase(
                IdentificationType.CC,
                "123456789"
        )).thenReturn(true);

        boolean result = accountService.existsByIdentification(
                IdentificationType.CC,
                "123456789"
        );

        assertTrue(result);

        verify(accountRepository).existsByIdentificationTypeAndIdentificationIgnoreCase(
                IdentificationType.CC,
                "123456789"
        );
    }

    @Test
    void existsByIdentification_ShouldReturnFalse_WhenIdentificationDataIsInvalid() {
        boolean result = accountService.existsByIdentification(
                IdentificationType.CC,
                "   "
        );

        assertFalse(result);

        verifyNoInteractions(accountRepository);
    }
    private Account captureBuiltAccount() {
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountMapper).toEntity(accountCaptor.capture());
        return accountCaptor.getValue();
    }

    private Account createAccountModel(String email, String passwordHash, Role role) {
        return validAccountBuilder(email)
                .passwordHash(passwordHash)
                .roles(List.of(role))
                .build();
    }

    private AccountEntity createAccountEntity(Long id) {
        AccountEntity entity = new AccountEntity();
        entity.setId(id);
        return entity;
    }

    private AccountResponse createAccountResponse(String email) {
        AccountResponse response = new AccountResponse();
        response.setEmail(email);
        return response;
    }

    private RoleEntity createRoleEntity(String roleName) {
        RoleEntity entity = new RoleEntity();
        entity.setName(roleName);
        return entity;
    }

    private Role createRole(Long id, String roleName) {
        Role role = new Role();
        role.setId(id);
        role.setName(roleName);
        return role;
    }

    private AccountAdminSearchCriteria createAccountAdminSearchCriteria(String query, String role, AccountStatus status, Integer page, Integer size, String sort){
        AccountAdminSearchCriteria criteria = new AccountAdminSearchCriteria();
        criteria.setQuery(query);
        criteria.setRole(role);
        criteria.setStatus(status);
        criteria.setPage(page);
        criteria.setSize(size);
        criteria.setSort(sort);
        return criteria;
    }

    private AccountAdminItemResponse createAccountAdminItemResponse (Long id, String fullName, String email, List<String> roles){
        AccountAdminItemResponse itemResponse = new AccountAdminItemResponse();
        itemResponse.setId(id);
        itemResponse.setFullName(fullName);
        itemResponse.setEmail(email);
        itemResponse.setRoles(roles);
        return itemResponse;
    }
}