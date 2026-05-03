package edu.eci.dosw.service;

import edu.eci.dosw.dto.*;
import edu.eci.dosw.entity.*;
import edu.eci.dosw.mapper.*;
import edu.eci.dosw.model.*;
import edu.eci.dosw.repository.*;
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
class AuthServiceTest {

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
    private RefreshTokenMapper refreshTokenMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(1L);

        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .id(1L)
                .createdAt(LocalDateTime.now())
                .addRole(new Role());
        Account account = accountBuilder.build();

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.of(accountEntity));

        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);

        when(passwordEncoder.matches("123456", "encoded-password"))
                .thenReturn(true);

        when(jwtService.generateRefreshToken(1L))
                .thenReturn("refresh-token");

        when(refreshTokenMapper.toEntity(any(RefreshToken.class)))
                .thenReturn(refreshTokenEntity);

        when(refreshTokenRepository.save(refreshTokenEntity))
                .thenReturn(refreshTokenEntity);

        when(jwtService.generateAccessToken(eq(1L), anyList(), anyList()))
                .thenReturn("access-token");

        when(jwtService.getAccessTokenExpiration())
                .thenReturn(3600000L); // 1 hora en ms

        // Act
        AuthResponse result = authService.login(request);

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600L, result.getExpiresIn());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verify(accountMapper).toModel(accountEntity);
        verify(passwordEncoder).matches("123456", "encoded-password");
        verify(jwtService).generateRefreshToken(1L);
        verify(jwtService).generateAccessToken(eq(1L), anyList(), anyList());
        verify(jwtService).getAccessTokenExpiration();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenMapper).toEntity(captor.capture());

        RefreshToken savedToken = captor.getValue();
        assertEquals("refresh-token", savedToken.getToken());
        assertFalse(savedToken.isRevoked());
        assertEquals(1L, savedToken.getAccount().getId());
    }

    @Test
    void login_ShouldThrowException_WhenAccountDoesNotExist() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail("noexiste@escuelaing.edu.co");
        request.setPassword("123456");

        when(accountRepository.findByEmail("noexiste@escuelaing.edu.co"))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        // Assert
        assertEquals("Invalid credentials", ex.getMessage());

        verify(accountRepository).findByEmail("noexiste@escuelaing.edu.co");
        verifyNoInteractions(accountMapper, passwordEncoder, jwtService, refreshTokenRepository, refreshTokenMapper);
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsInvalid() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");

        AccountEntity accountEntity = new AccountEntity();

        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .addRole(new Role());
        Account account = accountBuilder.build();

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.of(accountEntity));

        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);

        when(passwordEncoder.matches("123456", "encoded-password"))
                .thenReturn(false);

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        // Assert
        assertEquals("Invalid credentials", ex.getMessage());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verify(accountMapper).toModel(accountEntity);
        verify(passwordEncoder).matches("123456", "encoded-password");
        verifyNoInteractions(jwtService, refreshTokenRepository, refreshTokenMapper);
    }

    @Test
    void login_ShouldThrowException_WhenAccountIsInactive() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");

        AccountEntity accountEntity = new AccountEntity();

        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .addRole(new Role())
                .createdAt(LocalDateTime.now())
                .status("INACTIVE");
        Account account = accountBuilder.build();

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.of(accountEntity));

        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);

        when(passwordEncoder.matches("123456", "encoded-password"))
                .thenReturn(true);

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        // Assert
        assertEquals("Account not active", ex.getMessage());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verify(accountMapper).toModel(accountEntity);
        verify(passwordEncoder).matches("123456", "encoded-password");
        verifyNoInteractions(jwtService, refreshTokenRepository, refreshTokenMapper);
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenIsInvalid() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("bad-refresh-token");

        when(jwtService.isRefreshTokenValid("bad-refresh-token"))
                .thenReturn(false);

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.refreshToken(request));

        // Assert
        assertEquals("Invalid or expired refresh token", ex.getMessage());

        verify(jwtService).isRefreshTokenValid("bad-refresh-token");
        verifyNoInteractions(refreshTokenRepository, refreshTokenMapper, accountRepository, accountMapper);
    }

    @Test
    void refreshToken_ShouldThrowException_WhenStoredTokenDoesNotExist() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        when(jwtService.isRefreshTokenValid("refresh-token"))
                .thenReturn(true);

        when(refreshTokenRepository.findByToken("refresh-token"))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.refreshToken(request));

        // Assert
        assertEquals("Refresh token not found", ex.getMessage());

        verify(jwtService).isRefreshTokenValid("refresh-token");
        verify(refreshTokenRepository).findByToken("refresh-token");
    }

    @Test
    void refreshToken_ShouldThrowException_WhenStoredTokenIsRevoked() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();

        RefreshToken storedToken = new RefreshToken();
        storedToken.setToken("refresh-token");
        storedToken.setRevoked(true);

        when(jwtService.isRefreshTokenValid("refresh-token"))
                .thenReturn(true);

        when(refreshTokenRepository.findByToken("refresh-token"))
                .thenReturn(Optional.of(refreshTokenEntity));

        when(refreshTokenMapper.toModel(refreshTokenEntity))
                .thenReturn(storedToken);

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.refreshToken(request));

        // Assert
        assertEquals("Refresh token revoked", ex.getMessage());

        verify(jwtService).isRefreshTokenValid("refresh-token");
        verify(refreshTokenRepository).findByToken("refresh-token");
        verify(refreshTokenMapper).toModel(refreshTokenEntity);
    }

    @Test
    void logout_ShouldRevokeToken_WhenTokenExistsAndIsActive() {
        // Arrange
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .addRole(new Role());
        Account account = accountBuilder.build();

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        RefreshTokenEntity revokedEntity = new RefreshTokenEntity();

        RefreshToken storedToken = new RefreshToken();
        storedToken.setToken("refresh-token");
        storedToken.setRevoked(false);
        storedToken.setAccount(account);

        when(refreshTokenRepository.findByToken("refresh-token"))
                .thenReturn(Optional.of(refreshTokenEntity));

        when(refreshTokenMapper.toModel(refreshTokenEntity))
                .thenReturn(storedToken);

        when(refreshTokenMapper.toEntity(any(RefreshToken.class)))
                .thenReturn(revokedEntity);

        // Act
        authService.logout(request);

        // Assert
        verify(refreshTokenRepository).findByToken("refresh-token");
        verify(refreshTokenMapper).toModel(refreshTokenEntity);
        verify(refreshTokenMapper).toEntity(storedToken);
        verify(refreshTokenRepository).save(revokedEntity);

        assertTrue(storedToken.isRevoked());
    }

    @Test
    void logout_ShouldDoNothing_WhenTokenAlreadyRevoked() {
        // Arrange
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        AccountBuilder accountBuilder = new AccountBuilder();
        accountBuilder.email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .addRole(new Role());
        Account account = accountBuilder.build();

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();

        RefreshToken storedToken = new RefreshToken();
        storedToken.setToken("refresh-token");
        storedToken.setRevoked(true);
        storedToken.setAccount(account);

        when(refreshTokenRepository.findByToken("refresh-token"))
                .thenReturn(Optional.of(refreshTokenEntity));

        when(refreshTokenMapper.toModel(refreshTokenEntity))
                .thenReturn(storedToken);

        // Act
        authService.logout(request);

        // Assert
        verify(refreshTokenRepository).findByToken("refresh-token");
        verify(refreshTokenMapper).toModel(refreshTokenEntity);
        verify(refreshTokenRepository, never()).save(any());
        verify(refreshTokenMapper, never()).toEntity(any());
    }

    @Test
    void logout_ShouldThrowException_WhenTokenDoesNotExist() {
        // Arrange
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("missing-token");

        when(refreshTokenRepository.findByToken("missing-token"))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.logout(request));

        // Assert
        assertEquals("Refresh token not found", ex.getMessage());

        verify(refreshTokenRepository).findByToken("missing-token");
    }

    @Test
    void validateToken_ShouldReturnInvalidResponse_WhenTokenIsInvalid() {
        // Arrange
        TokenValidationRequest request = new TokenValidationRequest();
        request.setToken("bad-token");

        when(jwtService.isTokenValid("bad-token"))
                .thenReturn(false);

        // Act
        TokenValidationResponse result = authService.validateToken(request);

        // Assert
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNull(result.getAccountId());
        assertTrue(result.getRoles().isEmpty());
        assertTrue(result.getPermissions().isEmpty());
        assertNull(result.getTokenType());

        verify(jwtService).isTokenValid("bad-token");
        verifyNoMoreInteractions(jwtService);
        verifyNoInteractions(accountRepository, accountMapper);
    }
}