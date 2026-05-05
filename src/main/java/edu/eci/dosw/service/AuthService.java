package edu.eci.dosw.service;

import edu.eci.dosw.exception.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import edu.eci.dosw.dto.*;
import edu.eci.dosw.mapper.*;
import edu.eci.dosw.repository.*;
import edu.eci.dosw.model.*;
import edu.eci.dosw.entity.*;


@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final RoleService roleService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtService jwtService,
                       AccountRepository accountRepository,
                       AccountMapper accountMapper,
                       RoleService roleService,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.roleService = roleService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        Account account = findAccountByEmailOrThrow(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            log.warn("Login failed: invalid password for accountId={}", account.getId());
            throw new InvalidCredentialsException();
        }

        validateActiveAccount(account, "Login");

        String refreshToken = jwtService.generateRefreshToken(account.getId());
        saveRefreshToken(account, refreshToken);

        log.info("Login successful for accountId={}", account.getId());
        return buildAuthResponse(account, refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        if (!jwtService.isRefreshTokenValid(refreshTokenValue)) {
            log.warn("Refresh failed: invalid or expired refresh token");
            throw new InvalidRefreshTokenException();
        }

        RefreshTokenEntity storedToken = findRefreshTokenEntityOrThrow(refreshTokenValue, "Refresh");

        if (storedToken.isRevoked()) {
            log.warn("Refresh failed: refresh token revoked");
            throw new RefreshTokenRevokedException();
        }

        Long accountId = jwtService.extractUserId(refreshTokenValue);
        Account account = findAccountByIdOrThrow(accountId);

        validateActiveAccount(account, "Refresh");

        revokeRefreshToken(storedToken);

        String newRefreshToken = jwtService.generateRefreshToken(accountId);
        saveRefreshToken(account, newRefreshToken);

        log.info("Token refresh successful for accountId={}", account.getId());
        return buildAuthResponse(account, newRefreshToken);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        RefreshTokenEntity storedToken = findRefreshTokenEntityOrThrow(refreshTokenValue, "Logout");

        if (!storedToken.isRevoked()) {
            revokeRefreshToken(storedToken);
            log.info("Logout successful for accountId={}", storedToken.getAccount().getId());
        }
    }

    @Transactional
    public TokenValidationResponse validateToken(TokenValidationRequest request) {
        String token = request.getToken();

        if (!jwtService.isTokenValid(token)) {
            log.warn("Token validation failed");
            return invalidValidationResponse();
        }

        Long accountId;
        try {
            accountId = jwtService.extractUserId(token);
        } catch (NumberFormatException e) {
            log.warn("Token validation failed: invalid accountId format");
            return invalidValidationResponse();
        }

        List<String> roles = jwtService.extractRoles(token);
        List<String> permissions = jwtService.extractPermissions(token);
        String tokenType = jwtService.extractTokenType(token);

        Optional<AccountEntity> optionalAccountEntity = accountRepository.findById(accountId);
        if (optionalAccountEntity.isEmpty()) {
            log.warn("Token validation failed: account not found accountId={}", accountId);
            return invalidValidationResponse();
        }

        Account account = accountMapper.toModel(optionalAccountEntity.get());

        if (!account.isActive()) {
            log.warn("Token validation failed: inactive account accountId={}", accountId);
            return invalidValidationResponse();
        }

        return new TokenValidationResponse(true, accountId, roles, permissions, tokenType);
    }

    private Account findAccountByEmailOrThrow(String email) {
        AccountEntity accountEntity = accountRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: account not found for email={}", email);
                    return new InvalidCredentialsException();
                });

        return accountMapper.toModel(accountEntity);
    }

    private Account findAccountByIdOrThrow(Long accountId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return accountMapper.toModel(accountEntity);
    }

    private RefreshTokenEntity findRefreshTokenEntityOrThrow(String tokenValue, String action) {
        return refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> {
                    log.warn("{} failed: refresh token not found", action);
                    return new RefreshTokenNotFoundException();
                });
    }

    private void validateActiveAccount(Account account, String action) {
        if (!account.isActive()) {
            log.warn("{} failed: inactive account accountId={}", action, account.getId());
            throw new AccountNotActiveException(account.getId());
        }
    }

    private void revokeRefreshToken(RefreshTokenEntity tokenEntity) {
        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);
    }

    private void saveRefreshToken(Account account, String tokenValue) {
        AccountEntity accountEntity = accountRepository.findById(account.getId())
                .orElseThrow(() -> new AccountNotFoundException(account.getId()));

        RefreshTokenEntity tokenEntity = new RefreshTokenEntity();
        tokenEntity.setToken(tokenValue);
        tokenEntity.setAccount(accountEntity);
        tokenEntity.setRevoked(false);

        refreshTokenRepository.save(tokenEntity);
    }

    private AuthResponse buildAuthResponse(Account account, String refreshToken) {
        List<String> roleNames = extractRoleNames(account.getId());
        List<String> permissionNames = extractPermissionNames(account.getId());
        String accessToken = jwtService.generateAccessToken(account.getId(), roleNames, permissionNames);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiration() / 1000
        );
    }

    private TokenValidationResponse invalidValidationResponse() {
        return new TokenValidationResponse(
                false,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
    }

    private List<String> extractRoleNames(Long accountId) {
        List<Role> roles = roleService.getRolesByAccount(accountId);

        if (roles == null) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(Role::getName)
                .distinct()
                .toList();
    }

    private List<String> extractPermissionNames(Long accountId) {
        List<Role> roles = roleService.getRolesByAccount(accountId);

        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(Role::getId)
                .map(roleService::getPermissions)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(Permission::getName)
                .distinct()
                .toList();
    }
}
