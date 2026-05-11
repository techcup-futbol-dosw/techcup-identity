package edu.eci.dosw.unitary.service;

import edu.eci.dosw.dto.*;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.repository.AccountRepository;
import edu.eci.dosw.repository.RefreshTokenRepository;
import edu.eci.dosw.service.AuthService;
import edu.eci.dosw.service.JwtService;
import edu.eci.dosw.service.RoleService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;


import edu.eci.dosw.dto.LogoutRequest;
import edu.eci.dosw.dto.AuthRequest;
import edu.eci.dosw.dto.RefreshTokenRequest;
import edu.eci.dosw.dto.TokenValidationRequest;
import edu.eci.dosw.dto.TokenValidationResponse;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.RefreshTokenEntity;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountStatus;

import static edu.eci.dosw.testutil.TestDataFactory.playerRole;
import static edu.eci.dosw.testutil.TestDataFactory.validAccountBuilder;
import static edu.eci.dosw.testutil.TestDataFactory.validAccountEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Long ACCOUNT_ID = 1L;

    private static final String EMAIL = "juan@escuelaing.edu.co";
    private static final String MISSING_EMAIL = "noexiste@escuelaing.edu.co";
    private static final String PASSWORD = "123456";
    private static final String ENCODED_PASSWORD = "encoded-password";

    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String NEW_ACCESS_TOKEN = "new-access-token";
    private static final String NEW_REFRESH_TOKEN = "new-refresh-token";

    private static final String BAD_REFRESH_TOKEN = "bad-refresh-token";
    private static final String MISSING_REFRESH_TOKEN = "missing-token";

    private static final String BAD_TOKEN = "bad-token";
    private static final String VALID_TOKEN = "valid-token";
    private static final String BAD_FORMAT_TOKEN = "bad-format-token";

    private static final String TOKEN_TYPE = "Bearer";
    private static final String PLAYER_ROLE = "PLAYER";
    private static final String TOURNAMENT_READ_PERMISSION = "tournament:read";

    private static final long ACCESS_TOKEN_EXPIRATION = 3_600_000L;
    private static final long REFRESH_TOKEN_EXPIRATION = 604_800_000L;
    private static final long EXPIRES_IN_SECONDS = 3_600L;

    @Mock
    private JwtService jwtService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private RoleService roleService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        AuthRequest request = authRequest(EMAIL, PASSWORD);

        AccountEntity accountEntity = buildAccountEntity();
        Account account = buildAccount();

        mockExistingAccount(accountEntity, account);

        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD))
                .thenReturn(true);

        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(accountEntity));

        mockTokenGeneration(ACCESS_TOKEN, REFRESH_TOKEN);

        AuthResponse result = authService.login(request);

        assertAuthResponse(result, ACCESS_TOKEN, REFRESH_TOKEN);

        verify(jwtService).generateAccessToken(eq(ACCOUNT_ID), anyList(), anyList());
    }

    @Test
    void login_ShouldThrowException_WhenAccountDoesNotExist() {
        AuthRequest request = authRequest(MISSING_EMAIL, PASSWORD);

        when(accountRepository.findByEmail(MISSING_EMAIL))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid credentials", ex.getMessage());

        verifyNoInteractions(
                accountMapper,
                passwordEncoder,
                jwtService,
                refreshTokenRepository
        );
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsInvalid() {
        AuthRequest request = authRequest(EMAIL, PASSWORD);

        AccountEntity accountEntity = buildAccountEntity();
        Account account = buildAccount();

        mockExistingAccount(accountEntity, account);

        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD))
                .thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid credentials", ex.getMessage());

        verifyNoInteractions(jwtService, refreshTokenRepository);
    }

    @Test
    void login_ShouldThrowException_WhenAccountIsInactive() {
        AuthRequest request = authRequest(EMAIL, PASSWORD);

        AccountEntity accountEntity = buildAccountEntity();
        Account account = buildAccount(AccountStatus.INACTIVE);

        mockExistingAccount(accountEntity, account);

        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD))
                .thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals("Account is not active: 1", ex.getMessage());

        verifyNoInteractions(jwtService, refreshTokenRepository);
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenIsInvalid() {
        RefreshTokenRequest request = refreshRequest(BAD_REFRESH_TOKEN);

        when(jwtService.isRefreshTokenValid(BAD_REFRESH_TOKEN))
                .thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken(request)
        );

        assertEquals("Invalid or expired refresh token", ex.getMessage());

        verifyNoInteractions(
                refreshTokenRepository,
                accountRepository,
                accountMapper
        );
    }

    @Test
    void refreshToken_ShouldThrowException_WhenStoredTokenDoesNotExist() {
        RefreshTokenRequest request = refreshRequest(REFRESH_TOKEN);

        when(jwtService.isRefreshTokenValid(REFRESH_TOKEN))
                .thenReturn(true);

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken(request)
        );

        assertEquals("Refresh token not found", ex.getMessage());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenStoredTokenIsRevoked() {
        RefreshTokenRequest request = refreshRequest(REFRESH_TOKEN);

        RefreshTokenEntity refreshTokenEntity = refreshTokenEntity(true);

        when(jwtService.isRefreshTokenValid(REFRESH_TOKEN))
                .thenReturn(true);

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN))
                .thenReturn(Optional.of(refreshTokenEntity));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken(request)
        );

        assertEquals("Refresh token revoked or invalid", ex.getMessage());
    }

    @Test
    void refreshToken_ShouldReturnNewTokens_WhenTokenIsValid() {
        RefreshTokenRequest request = refreshRequest(REFRESH_TOKEN);

        AccountEntity accountEntity = buildAccountEntity();
        Account account = buildAccount();
        RefreshTokenEntity storedToken = refreshTokenEntity(false);

        when(jwtService.isRefreshTokenValid(REFRESH_TOKEN))
                .thenReturn(true);

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN))
                .thenReturn(Optional.of(storedToken));

        when(jwtService.extractUserId(REFRESH_TOKEN))
                .thenReturn(ACCOUNT_ID);

        mockAccountById(accountEntity, account);
        mockTokenGeneration(NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN);

        AuthResponse result = authService.refreshToken(request);

        assertAuthResponse(result, NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN);
        assertOldRefreshTokenWasRevoked();
    }

    @Test
    void logout_ShouldRevokeToken_WhenTokenExistsAndIsActive() {
        LogoutRequest request = logoutRequest(REFRESH_TOKEN);

        AccountEntity accountEntity = buildAccountEntity();
        RefreshTokenEntity refreshTokenEntity = refreshTokenEntityWithAccount(accountEntity);

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN))
                .thenReturn(Optional.of(refreshTokenEntity));

        authService.logout(request);

        assertTrue(refreshTokenEntity.isRevoked());

        verify(refreshTokenRepository).save(refreshTokenEntity);
    }

    @Test
    void logout_ShouldDoNothing_WhenTokenAlreadyRevoked() {
        LogoutRequest request = logoutRequest(REFRESH_TOKEN);

        RefreshTokenEntity refreshTokenEntity = refreshTokenEntity(true);

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN))
                .thenReturn(Optional.of(refreshTokenEntity));

        authService.logout(request);

        assertTrue(refreshTokenEntity.isRevoked());

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logout_ShouldThrowException_WhenTokenDoesNotExist() {
        LogoutRequest request = logoutRequest(MISSING_REFRESH_TOKEN);

        when(refreshTokenRepository.findByToken(MISSING_REFRESH_TOKEN))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.logout(request)
        );

        assertEquals("Refresh token not found", ex.getMessage());
    }

    @Test
    void validateToken_ShouldReturnInvalidResponse_WhenTokenIsInvalid() {
        TokenValidationRequest request = tokenValidationRequest(BAD_TOKEN);

        when(jwtService.isTokenValid(BAD_TOKEN))
                .thenReturn(false);

        TokenValidationResponse result = authService.validateToken(request);

        assertCompletelyInvalidTokenResponse(result);

        verify(jwtService).isTokenValid(BAD_TOKEN);
    }

    @Test
    void validateToken_ShouldReturnInvalid_WhenExtractUserIdThrowsNumberFormatException() {
        TokenValidationRequest request = tokenValidationRequest(BAD_FORMAT_TOKEN);

        when(jwtService.isTokenValid(BAD_FORMAT_TOKEN))
                .thenReturn(true);

        when(jwtService.extractUserId(BAD_FORMAT_TOKEN))
                .thenThrow(new NumberFormatException("invalid"));

        TokenValidationResponse result = authService.validateToken(request);

        assertInvalidTokenResponse(result);

        verify(jwtService).extractUserId(BAD_FORMAT_TOKEN);
    }

    @Test
    void validateToken_ShouldReturnInvalid_WhenAccountNotFound() {
        TokenValidationRequest request = tokenValidationRequest(VALID_TOKEN);

        mockValidTokenClaims();

        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.empty());

        TokenValidationResponse result = authService.validateToken(request);

        assertInvalidTokenResponse(result);
    }

    @Test
    void validateToken_ShouldReturnInvalid_WhenAccountIsInactive() {
        TokenValidationRequest request = tokenValidationRequest(VALID_TOKEN);

        AccountEntity accountEntity = buildAccountEntity();
        Account account = buildAccount(AccountStatus.INACTIVE);

        mockValidTokenClaims();
        mockAccountById(accountEntity, account);

        TokenValidationResponse result = authService.validateToken(request);

        assertFalse(result.isValid());
    }

    @Test
    void validateToken_ShouldReturnValid_WhenTokenAndAccountAreValid() {
        TokenValidationRequest request = tokenValidationRequest(VALID_TOKEN);

        AccountEntity accountEntity = buildAccountEntity();
        Account account = buildAccount();

        mockValidTokenClaims();
        mockAccountById(accountEntity, account);

        TokenValidationResponse result = authService.validateToken(request);

        assertTrue(result.isValid());
        assertEquals(ACCOUNT_ID, result.getAccountId());
        assertEquals(List.of(PLAYER_ROLE), result.getRoles());
        assertEquals(List.of(TOURNAMENT_READ_PERMISSION), result.getPermissions());
        assertEquals(TOKEN_TYPE, result.getTokenType());
    }

    private AuthRequest authRequest(String email, String password) {
        AuthRequest request = new AuthRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private RefreshTokenRequest refreshRequest(String token) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(token);
        return request;
    }

    private LogoutRequest logoutRequest(String token) {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken(token);
        return request;
    }

    private TokenValidationRequest tokenValidationRequest(String token) {
        TokenValidationRequest request = new TokenValidationRequest();
        request.setToken(token);
        return request;
    }

    private Account buildAccount() {
        return buildAccount(AccountStatus.ACTIVE);
    }

    private Account buildAccount(AccountStatus status) {
        return validAccountBuilder(EMAIL)
                .id(ACCOUNT_ID)
                .passwordHash(ENCODED_PASSWORD)
                .status(status)
                .addRole(playerRole())
                .build();
    }

    private AccountEntity buildAccountEntity() {
        AccountEntity entity = validAccountEntity(EMAIL);
        entity.setId(ACCOUNT_ID);
        return entity;
    }

    private RefreshTokenEntity refreshTokenEntity(boolean revoked) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setRevoked(revoked);
        return entity;
    }

    private RefreshTokenEntity refreshTokenEntityWithAccount(AccountEntity accountEntity) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setAccount(accountEntity);
        return entity;
    }

    private void mockExistingAccount(AccountEntity accountEntity, Account account) {
        when(accountRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(accountEntity));

        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);
    }

    private void mockAccountById(AccountEntity accountEntity, Account account) {
        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(accountEntity));

        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);
    }

    private void mockTokenGeneration(String accessToken, String refreshToken) {
        when(jwtService.generateRefreshToken(ACCOUNT_ID))
                .thenReturn(refreshToken);

        when(jwtService.getRefreshTokenExpiration())
                .thenReturn(REFRESH_TOKEN_EXPIRATION);

        when(refreshTokenRepository.save(any(RefreshTokenEntity.class)))
                .thenReturn(new RefreshTokenEntity());

        when(roleService.getRolesByAccount(ACCOUNT_ID))
                .thenReturn(List.of());

        when(jwtService.generateAccessToken(eq(ACCOUNT_ID), anyList(), anyList()))
                .thenReturn(accessToken);

        when(jwtService.getAccessTokenExpiration())
                .thenReturn(ACCESS_TOKEN_EXPIRATION);
    }

    private void mockValidTokenClaims() {
        when(jwtService.isTokenValid(VALID_TOKEN))
                .thenReturn(true);

        when(jwtService.extractUserId(VALID_TOKEN))
                .thenReturn(ACCOUNT_ID);

        when(jwtService.extractRoles(VALID_TOKEN))
                .thenReturn(List.of(PLAYER_ROLE));

        when(jwtService.extractPermissions(VALID_TOKEN))
                .thenReturn(List.of(TOURNAMENT_READ_PERMISSION));

        when(jwtService.extractTokenType(VALID_TOKEN))
                .thenReturn(TOKEN_TYPE);
    }

    private void assertAuthResponse(
            AuthResponse result,
            String expectedAccessToken,
            String expectedRefreshToken
    ) {
        assertNotNull(result);
        assertEquals(expectedAccessToken, result.getAccessToken());
        assertEquals(expectedRefreshToken, result.getRefreshToken());
        assertEquals(TOKEN_TYPE, result.getTokenType());
        assertEquals(EXPIRES_IN_SECONDS, result.getExpiresIn());
    }

    private void assertInvalidTokenResponse(TokenValidationResponse result) {
        assertFalse(result.isValid());
        assertNull(result.getAccountId());
    }

    private void assertCompletelyInvalidTokenResponse(TokenValidationResponse result) {
        assertInvalidTokenResponse(result);
        assertTrue(result.getRoles().isEmpty());
        assertTrue(result.getPermissions().isEmpty());
        assertNull(result.getTokenType());
    }

    private void assertOldRefreshTokenWasRevoked() {
        ArgumentCaptor<RefreshTokenEntity> captor =
                ArgumentCaptor.forClass(RefreshTokenEntity.class);

        verify(refreshTokenRepository, atLeastOnce())
                .save(captor.capture());

        assertTrue(
                captor.getAllValues()
                        .stream()
                        .anyMatch(RefreshTokenEntity::isRevoked)
        );
    }
}