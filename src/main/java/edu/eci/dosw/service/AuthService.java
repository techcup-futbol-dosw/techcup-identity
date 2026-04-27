package edu.eci.dosw.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AuthService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthService(JwtService jwtService, UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository){
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }
    @Transactional
    public AuthResponse login(AuthRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for email={}", request.getEmail());
                    return new RuntimeException("Invalid credentials");
                });
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            log.warn("Login failed: invalid password for userId={}", user.getId());
            throw new RuntimeException("Invalid credentials");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())){
            log.warn("Login failed: inactive user userId={}", user.getId());
            throw new RuntimeException("User not active");
        }
        List<String> roleNames = extractRoleNames(user.getId());
        List<String> permissionNames = extractPermissionNames(user.getId());
        String accessToken = jwtService.generateAccessToken(user.getId(), roleNames, permissionNames);
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        saveRefreshToken(user, refreshToken);
        log.info("Login successful for userId={}", user.getId());
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiration() / 1000
        );
    }
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request){
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            log.warn("Refresh failed: invalid or expired refresh token");
            throw new RuntimeException("Invalid or expired refresh token");
        }
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh failed: refresh token not found");
                    return new RuntimeException("Refresh token not found");
                });
        if (storedToken.isRevoked()){
            log.warn("Refresh failed: refresh token revoked");
            throw new RuntimeException("Refresh token revoked");
        }
        Long userId = Long.valueOf(jwtService.extractUserId(refreshToken));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())){
            log.warn("Refresh failed: inactive user userId={}", user.getId());
            throw new RuntimeException("User not active");
        }
        List<String> roleNames = extractRoleNames(user.getId());
        List<String> permissionNames = extractPermissionNames(user.getId());
        String newAccessToken = jwtService.generateAccessToken(user.getId(), roleNames, permissionNames);
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        String newRefreshToken = jwtService.generateRefreshToken(userId);
        saveRefreshToken(user, newRefreshToken);
        log.info("Token refresh successful for userId={}", user.getId());
        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiration() / 1000
        );
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> {
                    log.warn("Logout failed: refresh token not found");
                    return new RuntimeException("Refresh token not found");
                });
        if (!storedToken.isRevoked()) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            log.info("Logout successful for userId={}", storedToken.getUser().getId());
        }
    }
    @Transactional
    public TokenValidationResponse validateToken(TokenValidationRequest request){
        String token = request.getToken();
        boolean valid = jwtService.isTokenValid(token);
        if (!valid){
            log.warn("Token validation failed");
            return new TokenValidationResponse(
                    false,
                    null,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    null
            );
        }
        String userId = jwtService.extractUserId(token);
        List<String> roles = jwtService.extractRoles(token);
        List<String> permissions = jwtService.extractPermissions(token);
        String tokenType = jwtService.extractTokenType(token);
        Optional<User> optionalUser = userRepository.findById(Long.valueOf(userId));
        if (optionalUser.isEmpty() || !"ACTIVE".equalsIgnoreCase(optionalUser.get().getStatus())) {
            log.warn("Token validation failed: user not found or inactive userId={}", userId);
            return new TokenValidationResponse(
                    false,
                    null,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    null
            );
        }
        return new TokenValidationResponse(true, userId, roles, permissions, tokenType);
    }
    private List<String> extractRoleNames(Long userId) {
        List<Role> roles = roleService.getRoleByUser(userId);
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(Role::getName)
                .distinct()
                .toList();
    }

    private List<String> extractPermissionNames(Long userId) {
        List<Role> roles = roleService.getRoleByUser(userId);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(Role::getId)
                .map(roleService::getPermissions)
                .filter(permissionList -> permissionList != null)
                .flatMap(List::stream)
                .map(Permission::getName)
                .distinct()
                .toList();
    }
}
