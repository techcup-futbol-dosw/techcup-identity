package edu.eci.dosw.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.dto.AuthRequest;
import edu.eci.dosw.dto.LogoutRequest;
import edu.eci.dosw.dto.RefreshTokenRequest;
import edu.eci.dosw.dto.TokenValidationRequest;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.RefreshTokenEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountStatus;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static edu.eci.dosw.testutil.TestDataFactory.validAccountBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    private static final String PLAYER_ROLE = "PLAYER";

    private static final String DEFAULT_PASSWORD = "Password123*";
    private static final String WRONG_PASSWORD = "WrongPassword";

    private static final String LOGIN_PATH = "/auth/login";
    private static final String REFRESH_PATH = "/auth/refresh";
    private static final String LOGOUT_PATH = "/auth/logout";
    private static final String VALIDATE_PATH = "/auth/validate";

    private static final String TOKEN_TYPE_BEARER = "Bearer";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";

    private static final String ACCOUNT_READ_SELF = "account:read:self";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private JwtService jwtService;

    private RoleEntity playerRoleEntity;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        playerRoleEntity = roleRepository.save(createRoleEntity(PLAYER_ROLE));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        String email = "login@mail.escuelaing.edu.co";

        createPersistedAccount(
                email,
                DEFAULT_PASSWORD,
                AccountStatus.ACTIVE,
                playerRoleEntity
        );

        performLogin(email, DEFAULT_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value(TOKEN_TYPE_BEARER))
                .andExpect(jsonPath("$.expiresIn").isNumber());

        assertThat(refreshTokenRepository.findAll()).hasSize(1);
        assertThat(refreshTokenRepository.findAll().get(0).isRevoked()).isFalse();
    }

    @Test
    @DisplayName("Should return 401 when password is invalid")
    void shouldReturnUnauthorizedWhenPasswordIsInvalid() throws Exception {
        String email = "wrongpass@mail.escuelaing.edu.co";

        createPersistedAccount(
                email,
                DEFAULT_PASSWORD,
                AccountStatus.ACTIVE,
                playerRoleEntity
        );

        performLogin(email, WRONG_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() throws Exception {
        String email = "refresh@mail.escuelaing.edu.co";

        createPersistedAccount(
                email,
                DEFAULT_PASSWORD,
                AccountStatus.ACTIVE,
                playerRoleEntity
        );

        String refreshToken = loginAndExtractRefreshToken(email);

        String refreshResponse = performRefresh(refreshToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String newRefreshToken = extractRefreshToken(refreshResponse);

        assertThat(newRefreshToken).isNotEqualTo(refreshToken);

        RefreshTokenEntity oldStoredToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow();

        assertThat(oldStoredToken.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should return 401 when refresh token is revoked")
    void shouldReturnUnauthorizedWhenRefreshTokenIsRevoked() throws Exception {
        String email = "revoked@mail.escuelaing.edu.co";

        createPersistedAccount(
                email,
                DEFAULT_PASSWORD,
                AccountStatus.ACTIVE,
                playerRoleEntity
        );

        String refreshToken = loginAndExtractRefreshToken(email);

        performLogout(refreshToken)
                .andExpect(status().isNoContent());

        performRefresh(refreshToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Should revoke refresh token on logout")
    void shouldRevokeRefreshTokenOnLogout() throws Exception {
        String email = "logout@mail.escuelaing.edu.co";

        createPersistedAccount(
                email,
                DEFAULT_PASSWORD,
                AccountStatus.ACTIVE,
                playerRoleEntity
        );

        String refreshToken = loginAndExtractRefreshToken(email);

        performLogout(refreshToken)
                .andExpect(status().isNoContent());

        RefreshTokenEntity storedToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow();

        assertThat(storedToken.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateTokenSuccessfully() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "validate@mail.escuelaing.edu.co",
                DEFAULT_PASSWORD,
                AccountStatus.ACTIVE,
                playerRoleEntity
        );

        String accessToken = jwtService.generateAccessToken(
                accountEntity.getId(),
                List.of(PLAYER_ROLE),
                List.of(ACCOUNT_READ_SELF)
        );

        performValidate(accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.accountId").value(accountEntity.getId()))
                .andExpect(jsonPath("$.roles[0]").value(PLAYER_ROLE))
                .andExpect(jsonPath("$.permissions[0]").value(ACCOUNT_READ_SELF))
                .andExpect(jsonPath("$.tokenType").value(ACCESS_TOKEN_TYPE));
    }

    @Test
    @DisplayName("Should return 401 when account is inactive")
    void shouldReturnUnauthorizedWhenAccountIsInactive() throws Exception {
        String email = "inactive@mail.escuelaing.edu.co";

        createPersistedAccount(
                email,
                DEFAULT_PASSWORD,
                AccountStatus.INACTIVE,
                playerRoleEntity
        );

        performLogin(email, DEFAULT_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").exists());
    }

    private AccountEntity createPersistedAccount(String email,
                                                 String rawPassword,
                                                 AccountStatus status,
                                                 RoleEntity roleEntity) {
        Role roleModel = roleMapper.toModel(roleEntity);

        Account account = validAccountBuilder(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(status)
                .addRole(roleModel)
                .build();

        return accountRepository.save(accountMapper.toEntity(account));
    }

    private RoleEntity createRoleEntity(String roleName) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(roleName);
        roleEntity.setPermissions(new ArrayList<>());
        return roleEntity;
    }

    private ResultActions performLogin(String email, String password) throws Exception {
        return performPost(LOGIN_PATH, createAuthRequest(email, password));
    }

    private ResultActions performRefresh(String refreshToken) throws Exception {
        return performPost(REFRESH_PATH, createRefreshRequest(refreshToken));
    }

    private ResultActions performLogout(String refreshToken) throws Exception {
        return performPost(LOGOUT_PATH, createLogoutRequest(refreshToken));
    }

    private ResultActions performValidate(String token) throws Exception {
        return performPost(VALIDATE_PATH, createTokenValidationRequest(token));
    }

    private ResultActions performPost(String path, Object request) throws Exception {
        return mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private String loginAndExtractRefreshToken(String email) throws Exception {
        String loginResponse = performLogin(email, DEFAULT_PASSWORD)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractRefreshToken(loginResponse);
    }

    private String extractRefreshToken(String responseBody) throws Exception {
        JsonNode json = objectMapper.readTree(responseBody);
        return json.get("refreshToken").asText();
    }

    private AuthRequest createAuthRequest(String email, String password) {
        AuthRequest request = new AuthRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private RefreshTokenRequest createRefreshRequest(String refreshToken) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);
        return request;
    }

    private LogoutRequest createLogoutRequest(String refreshToken) {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken(refreshToken);
        return request;
    }

    private TokenValidationRequest createTokenValidationRequest(String token) {
        TokenValidationRequest request = new TokenValidationRequest();
        request.setToken(token);
        return request;
    }
}

