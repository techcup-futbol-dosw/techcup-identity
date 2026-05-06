package edu.eci.dosw.unitaria.service;

import edu.eci.dosw.dto.*;
import edu.eci.dosw.entity.*;
import edu.eci.dosw.entity.AccountStatus;
import edu.eci.dosw.mapper.*;
import edu.eci.dosw.model.*;
import edu.eci.dosw.repository.*;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        AuthRequest request = new AuthRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(1L);

        Account account = new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .id(1L)
                .createdAt(LocalDateTime.now())
                .addRole(new Role())
                .build();

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.of(accountEntity));
        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);
        when(passwordEncoder.matches("123456", "encoded-password"))
                .thenReturn(true);
        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(accountEntity));
        when(jwtService.generateRefreshToken(1L))
                .thenReturn("refresh-token");
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class)))
                .thenReturn(new RefreshTokenEntity());
        when(roleService.getRolesByAccount(1L))
                .thenReturn(List.of());
        when(jwtService.generateAccessToken(eq(1L), anyList(), anyList()))
                .thenReturn("access-token");
        when(jwtService.getAccessTokenExpiration())
                .thenReturn(3600000L);

        AuthResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600L, result.getExpiresIn());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verify(accountMapper).toModel(accountEntity);
        verify(passwordEncoder).matches("123456", "encoded-password");
        verify(accountRepository).findById(1L);
        verify(jwtService).generateRefreshToken(1L);
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
        verify(jwtService).generateAccessToken(eq(1L), anyList(), anyList());
    }

    @Test
    void login_ShouldThrowException_WhenAccountDoesNotExist() {
        AuthRequest request = new AuthRequest();
        request.setEmail("noexiste@escuelaing.edu.co");
        request.setPassword("123456");

        when(accountRepository.findByEmail("noexiste@escuelaing.edu.co"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Invalid credentials", ex.getMessage());

        verify(accountRepository).findByEmail("noexiste@escuelaing.edu.co");
        verifyNoInteractions(accountMapper, passwordEncoder, jwtService, refreshTokenRepository);
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsInvalid() {
        AuthRequest request = new AuthRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(1L);

        Account account = new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .createdAt(LocalDateTime.now())
                .addRole(new Role())
                .build();

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.of(accountEntity));
        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);
        when(passwordEncoder.matches("123456", "encoded-password"))
                .thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Invalid credentials", ex.getMessage());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verify(accountMapper).toModel(accountEntity);
        verify(passwordEncoder).matches("123456", "encoded-password");
        verifyNoInteractions(jwtService, refreshTokenRepository);
    }

    @Test
    void login_ShouldThrowException_WhenAccountIsInactive() {
        AuthRequest request = new AuthRequest();
        request.setEmail("juan@escuelaing.edu.co");
        request.setPassword("123456");

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(1L);

        Account account = new AccountBuilder()
                .email("juan@escuelaing.edu.co")
                .passwordHash("encoded-password")
                .addRole(new Role())
                .createdAt(LocalDateTime.now())
                .status(AccountStatus.INACTIVE)
                .build();

        when(accountRepository.findByEmail("juan@escuelaing.edu.co"))
                .thenReturn(Optional.of(accountEntity));
        when(accountMapper.toModel(accountEntity))
                .thenReturn(account);
        when(passwordEncoder.matches("123456", "encoded-password"))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Account not active", ex.getMessage());

        verify(accountRepository).findByEmail("juan@escuelaing.edu.co");
        verify(accountMapper).toModel(accountEntity);
        verify(passwordEncoder).matches("123456", "encoded-password");
        verifyNoInteractions(jwtService, refreshTokenRepository);
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenIsInvalid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("bad-refresh-token");

        when(jwtService.isRefreshTokenValid("bad-refresh-token"))
                .thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.refreshToken(request));

        assertEquals("Invalid or expired refresh token", ex.getMessage());

        verify(jwtService).isRefreshTokenValid("bad-refresh-token");
        verifyNoInteractions(refreshTokenRepository, accountRepository, accountMapper);
    }

    @Test
    void refreshToken_ShouldThrowException_WhenStoredTokenDoesNotExist() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        when(jwtService.isRefreshTokenValid("refresh-token"))
                .thenReturn(true);
        when(refreshTokenRepository.findByToken("refresh-token"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.refreshToken(request));

        assertEquals("Refresh token not found", ex.getMessage());

        verify(jwtService).isRefreshTokenValid("refresh-token");
        verify(refreshTokenRepository).findByToken("refresh-token");
    }

    @Test
    void refreshToken_ShouldThrowException_WhenStoredTokenIsRevoked() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setRevoked(true);

        when(jwtService.isRefreshTokenValid("refresh-token"))
                .thenReturn(true);
        when(refreshTokenRepository.findByToken("refresh-token"))
                .thenReturn(Optional.of(refreshTokenEntity));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.refreshToken(request));

        assertEquals("Refresh token revoked", ex.getMessage());

        verify(jwtService).isRefreshTokenValid("refresh-token");
        verify(refreshTokenRepository).findByToken("refresh-token");
    }
    
    @Test
    void logout_ShouldRevokeToken_WhenTokenExistsAndIsActive() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(1L);

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setAccount(accountEntity); // NUEVO

        when(refreshTokenRepository.findByToken("refresh-token"))
                .thenReturn(Optional.of(refreshTokenEntity));

        authService.logout(request);

        verify(refreshTokenRepository).findByToken("refresh-token");
        verify(refreshTokenRepository).save(refreshTokenEntity);
    }

    @Test
    void logout_ShouldDoNothing_WhenTokenAlreadyRevoked() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setRevoked(true);

        when(refreshTokenRepository.findByToken("refresh-token"))
                .thenReturn(Optional.of(refreshTokenEntity));

        authService.logout(request);

        verify(refreshTokenRepository).findByToken("refresh-token");
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logout_ShouldThrowException_WhenTokenDoesNotExist() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("missing-token");

        when(refreshTokenRepository.findByToken("missing-token"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.logout(request));

        assertEquals("Refresh token not found", ex.getMessage());

        verify(refreshTokenRepository).findByToken("missing-token");
    }

    @Test
    void validateToken_ShouldReturnInvalidResponse_WhenTokenIsInvalid() {
        TokenValidationRequest request = new TokenValidationRequest();
        request.setToken("bad-token");

        when(jwtService.isTokenValid("bad-token"))
                .thenReturn(false);

        TokenValidationResponse result = authService.validateToken(request);

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