package edu.eci.dosw.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.client.TeamClient;
import edu.eci.dosw.client.TournamentClient;
import edu.eci.dosw.client.UserClient;
import edu.eci.dosw.dto.RegisterAccountRequest;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.Role;
import edu.eci.dosw.repository.AccountRepository;
import edu.eci.dosw.repository.RefreshTokenRepository;
import edu.eci.dosw.repository.RoleRepository;
import edu.eci.dosw.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static edu.eci.dosw.testutil.TestDataFactory.validAccountBuilder;
import static edu.eci.dosw.testutil.TestDataFactory.validRegisterRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

    private static final String PLAYER = "PLAYER";
    private static final String ADMIN = "ADMIN";

    private static final String DEFAULT_PASSWORD = "Password123*";

    private static final String ACCOUNT_READ_SELF = "account:read:self";
    private static final String ACCOUNT_READ_ANY = "account:read:any";
    private static final String ACCOUNT_DEACTIVATE_ANY = "account:deactivate:any";

    private static final Long ADMIN_ACCOUNT_ID = 999L;
    private static final Long MISSING_ACCOUNT_ID = 99999L;

    private static final String REGISTER_PATH = "/accounts/register";
    private static final String EXISTS_PATH = "/accounts/exists";
    private static final String ACCOUNT_BY_ID_PATH = "/accounts/{id}";
    private static final String DEACTIVATE_PATH = "/accounts/{id}/deactivate";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private RoleEntity playerRoleEntity;
    private RoleEntity adminRoleEntity;

    @MockBean
    private TeamClient teamClient;

    @MockBean
    private TournamentClient tournamentClient;

    @MockBean
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        playerRoleEntity = roleRepository.save(roleEntity(PLAYER));
        adminRoleEntity = roleRepository.save(roleEntity(ADMIN));
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @Test
    @DisplayName("Should register account successfully")
    void shouldRegisterAccountSuccessfully() throws Exception {
        String email = "juan@mail.escuelaing.edu.co";
        RegisterAccountRequest request = validRegisterRequest(email);

        performRegister(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Roa"))
                .andExpect(jsonPath("$.relation").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.semester").value(7))
                .andExpect(jsonPath("$.gender").value("MALE"));

        assertThat(accountRepository.findByEmail(email)).isPresent();
    }

    @Test
    @DisplayName("Should return 409 when email is already registered")
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        RegisterAccountRequest request = validRegisterRequest("juan@mail.escuelaing.edu.co");

        performRegister(request)
                .andExpect(status().isCreated());

        performRegister(request)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 when student semester is missing")
    void shouldReturnBadRequestWhenStudentSemesterIsMissing() throws Exception {
        RegisterAccountRequest request = validRegisterRequest("maria@mail.escuelaing.edu.co");
        request.setSemester(null);

        performRegister(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 when family email is not Gmail")
    void shouldReturnBadRequestWhenFamilyEmailIsNotGmail() throws Exception {
        RegisterAccountRequest request = validRegisterRequest("family@mail.escuelaing.edu.co");
        request.setRelation(Relation.FAMILIAR);

        performRegister(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 when institutional email is invalid")
    void shouldReturnBadRequestWhenInstitutionalEmailIsInvalid() throws Exception {
        RegisterAccountRequest request = validRegisterRequest("juan@gmail.com");
        request.setRelation(Relation.ESTUDIANTE);

        performRegister(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    // =========================================================
    // EXISTS BY EMAIL
    // =========================================================

    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrueWhenEmailExists() throws Exception {
        String email = "exists@mail.escuelaing.edu.co";
        createPersistedAccount(email, playerRoleEntity);

        mockMvc.perform(get(EXISTS_PATH).param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() throws Exception {
        mockMvc.perform(get(EXISTS_PATH).param("email", "missing@mail.escuelaing.edu.co"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Test
    @DisplayName("Should allow self account read")
    void shouldAllowSelfAccountRead() throws Exception {
        String email = "self@mail.escuelaing.edu.co";
        AccountEntity accountEntity = createPersistedAccount(email, playerRoleEntity);

        String token = accessToken(
                accountEntity.getId(),
                List.of(PLAYER),
                List.of(ACCOUNT_READ_SELF)
        );

        mockMvc.perform(get(ACCOUNT_BY_ID_PATH, accountEntity.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("Should allow admin to read any account")
    void shouldAllowAdminToReadAnyAccount() throws Exception {
        String email = "user@mail.escuelaing.edu.co";
        AccountEntity accountEntity = createPersistedAccount(email, playerRoleEntity);

        String token = adminToken(ACCOUNT_READ_ANY);

        mockMvc.perform(get(ACCOUNT_BY_ID_PATH, accountEntity.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("Should return 403 when user tries to read another account without read:any")
    void shouldReturnForbiddenWhenReadingAnotherAccountWithoutPermission() throws Exception {
        AccountEntity ownerAccount = createPersistedAccount("owner@mail.escuelaing.edu.co", playerRoleEntity);
        AccountEntity otherAccount = createPersistedAccount("other@mail.escuelaing.edu.co", playerRoleEntity);

        String token = accessToken(
                ownerAccount.getId(),
                List.of(PLAYER),
                List.of(ACCOUNT_READ_SELF)
        );

        mockMvc.perform(get(ACCOUNT_BY_ID_PATH, otherAccount.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when account does not exist")
    void shouldReturnNotFoundWhenAccountDoesNotExist() throws Exception {
        String token = adminToken(ACCOUNT_READ_ANY);

        mockMvc.perform(get(ACCOUNT_BY_ID_PATH, MISSING_ACCOUNT_ID).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Test
    @DisplayName("Should deactivate account when caller has permission")
    void shouldDeactivateAccountWhenCallerHasPermission() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "deactivate@mail.escuelaing.edu.co",
                playerRoleEntity
        );

        String token = adminToken(ACCOUNT_DEACTIVATE_ANY);
        String authorizationHeader = bearer(token);

        when(teamClient.findTeamIdByPlayerId(accountEntity.getId(), authorizationHeader))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch(DEACTIVATE_PATH, accountEntity.getId())
                        .header("Authorization", authorizationHeader))
                .andExpect(status().isNoContent());

        AccountEntity updated = accountRepository.findById(accountEntity.getId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(AccountStatus.INACTIVE);

        verify(teamClient).findTeamIdByPlayerId(accountEntity.getId(), authorizationHeader);
        verifyNoInteractions(tournamentClient);
        verify(userClient).deactivateUser(accountEntity.getId(), authorizationHeader);
    }


    @Test
    @DisplayName("Should return 403 when caller tries to deactivate without permission")
    void shouldReturnForbiddenWhenDeactivatingWithoutPermission() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "nodeactivate@mail.escuelaing.edu.co",
                playerRoleEntity
        );

        String token = accessToken(
                ADMIN_ACCOUNT_ID,
                List.of(PLAYER),
                List.of(ACCOUNT_READ_SELF)
        );

        mockMvc.perform(patch(DEACTIVATE_PATH, accountEntity.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private ResultActions performRegister(RegisterAccountRequest request) throws Exception {
        return mockMvc.perform(post(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private AccountEntity createPersistedAccount(String email, RoleEntity roleEntity) {
        Role roleModel = roleMapper.toModel(roleEntity);

        Account account = validAccountBuilder(email)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .status(AccountStatus.ACTIVE)
                .addRole(roleModel)
                .build();

        return accountRepository.save(accountMapper.toEntity(account));
    }

    private RoleEntity roleEntity(String roleName) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(roleName);
        roleEntity.setPermissions(new ArrayList<>());
        return roleEntity;
    }

    private String adminToken(String permission) {
        return accessToken(
                ADMIN_ACCOUNT_ID,
                List.of(ADMIN),
                List.of(permission)
        );
    }

    private String accessToken(Long accountId, List<String> roles, List<String> permissions) {
        return jwtService.generateAccessToken(accountId, roles, permissions);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}