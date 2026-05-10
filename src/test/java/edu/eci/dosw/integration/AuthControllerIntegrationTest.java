package edu.eci.dosw.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.dto.*;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.entity.RefreshTokenEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

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

        playerRoleEntity = new RoleEntity();
        playerRoleEntity.setName("PLAYER");
        playerRoleEntity.setPermissions(new ArrayList<>());
        playerRoleEntity = roleRepository.save(playerRoleEntity);
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        createPersistedAccount("login@mail.escuelaing.edu.co", "Password123*", AccountStatus.ACTIVE, playerRoleEntity);

        AuthRequest request = new AuthRequest();
        request.setEmail("login@mail.escuelaing.edu.co");
        request.setPassword("Password123*");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());

        assertThat(refreshTokenRepository.findAll()).hasSize(1);
        assertThat(refreshTokenRepository.findAll().get(0).isRevoked()).isFalse();
    }

    @Test
    @DisplayName("Should return 401 when password is invalid")
    void shouldReturnUnauthorizedWhenPasswordIsInvalid() throws Exception {
        createPersistedAccount("wrongpass@mail.escuelaing.edu.co", "Password123*", AccountStatus.ACTIVE, playerRoleEntity);

        AuthRequest request = new AuthRequest();
        request.setEmail("wrongpass@mail.escuelaing.edu.co");
        request.setPassword("WrongPassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() throws Exception {
        createPersistedAccount("refresh@mail.escuelaing.edu.co", "Password123*", AccountStatus.ACTIVE, playerRoleEntity);

        // first login
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("refresh@mail.escuelaing.edu.co");
        loginRequest.setPassword("Password123*");

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String refreshToken = loginJson.get("refreshToken").asText();

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        String refreshResponse = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode refreshJson = objectMapper.readTree(refreshResponse);
        String newRefreshToken = refreshJson.get("refreshToken").asText();

        assertThat(newRefreshToken).isNotEqualTo(refreshToken);

        // old token should now be revoked in DB
        RefreshTokenEntity oldStoredToken = refreshTokenRepository.findByToken(refreshToken).orElseThrow();
        assertThat(oldStoredToken.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should return 401 when refresh token is revoked")
    void shouldReturnUnauthorizedWhenRefreshTokenIsRevoked() throws Exception {
        createPersistedAccount("revoked@mail.escuelaing.edu.co", "Password123*", AccountStatus.ACTIVE, playerRoleEntity);

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("revoked@mail.escuelaing.edu.co");
        loginRequest.setPassword("Password123*");

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String refreshToken = loginJson.get("refreshToken").asText();

        // logout revokes refresh token
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        // try refresh again with revoked token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Should revoke refresh token on logout")
    void shouldRevokeRefreshTokenOnLogout() throws Exception {
        createPersistedAccount("logout@mail.escuelaing.edu.co", "Password123*", AccountStatus.ACTIVE, playerRoleEntity);

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("logout@mail.escuelaing.edu.co");
        loginRequest.setPassword("Password123*");

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String refreshToken = loginJson.get("refreshToken").asText();

        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        RefreshTokenEntity storedToken = refreshTokenRepository.findByToken(refreshToken).orElseThrow();
        assertThat(storedToken.isRevoked()).isTrue();
    }
    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateTokenSuccessfully() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "validate@mail.escuelaing.edu.co",
                "Password123*",
                AccountStatus.ACTIVE,
                playerRoleEntity
        );

        String accessToken = jwtService.generateAccessToken(
                accountEntity.getId(),
                List.of("PLAYER"),
                List.of("account:read:self")
        );

        TokenValidationRequest request = new TokenValidationRequest();
        request.setToken(accessToken);

        mockMvc.perform(post("/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.accountId").value(accountEntity.getId()))
                .andExpect(jsonPath("$.roles[0]").value("PLAYER"))
                .andExpect(jsonPath("$.permissions[0]").value("account:read:self"))
                .andExpect(jsonPath("$.tokenType").value("ACCESS"));
    }
    @Test
    @DisplayName("Should return 401 when account is inactive")
    void shouldReturnUnauthorizedWhenAccountIsInactive() throws Exception {
        createPersistedAccount(
                "inactive@mail.escuelaing.edu.co",
                "Password123*",
                AccountStatus.INACTIVE,
                playerRoleEntity
        );

        AuthRequest request = new AuthRequest();
        request.setEmail("inactive@mail.escuelaing.edu.co");
        request.setPassword("Password123*");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").exists());
    }
    private AccountEntity createPersistedAccount(String email,
                                                 String rawPassword,
                                                 AccountStatus status,
                                                 RoleEntity roleEntity) {
        Role roleModel = roleMapper.toModel(roleEntity);

        LocalDateTime now = LocalDateTime.now();

        Account account = new AccountBuilder()
                .name("Juan")
                .lastName("Roa")
                .birthDate(LocalDate.of(2000, 5, 15))
                .relation(Relation.ESTUDIANTE)
                .semester(7)
                .program("SISTEMAS")
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .gender(Gender.MALE)
                .identificationType(IdentificationType.CC)
                .identification("AUTH-" + Math.abs(email.hashCode()))
                .addRole(roleModel)
                .build();

        return accountRepository.save(accountMapper.toEntity(account));
    }
}