package edu.eci.dosw.service;

import jakarta.transaction.Transactional;
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
                .orElseThrow(()-> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new RuntimeException("Invalid credentials");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())){
            throw new RuntimeException("User not active");
        }
        List<String> roleNames = extractRoleNames(user.getId());
        List<String> permissionNames = extractPermissionNames(user.getId());
        String accessToken = jwtService.generateAccessToken(user.getId(), roleNames, permissionNames);
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        saveRefreshToken(user, refreshToken);
        return new AuthResponse(accessToken, refreshToken, "Bearer", jwtService.getAccessTokenExpiration()/1000); //expiración en segundos
    }
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request){
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("\"Refresh token not found"));
        if (storedToken.isRevoked()){
            throw new RuntimeException("Refresh token revoked");
        }
        Long userId = Long.valueOf(jwtService.extractUserId(refreshToken));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())){
            throw new RuntimeException("User not active");
        }
        List<String> roleNames = extractRoleNames(user.getId());
        List<String> permissionNames = extractPermissionNames(user.getId());
        String newAccessToken = jwtService.generateAccessToken(user.getId(), roleNames, permissionNames);
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        String newRefreshToken = jwtService.generateRefreshToken(userId);
        saveRefreshToken(user, newRefreshToken);
        return new AuthResponse(newAccessToken, newRefreshToken, "Bearer", jwtService.getAccessTokenExpiration()/1000); // expiración en segundos
    }
    @Transactional
    public void logout(LogoutRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (!storedToken.isRevoked()) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
        }
    }
    @Transactional
    public TokenValidationResponse validateToken(TokenValidationRequest request){
        String token = request.getToken();
        boolean valid = jwtService.isTokenValid(token);
        if (!valid){
            return new TokenValidationResponse(false, null, Collections.emptyList(),Collections.emptyList(), null);
        }
        String userId = jwtService.extractUserId(token);
        List<String> roles = jwtService.extractRoles(token);
        List<String> permissions = jwtService.extractPermissions(token);
        String tokenType = jwtService.extractTokenType(token);
        Optional<User> optionalUser = userRepository.findById(Long.valueOf(userId));
        if (optionalUser.isEmpty() || !"ACTIVE".equalsIgnoreCase(optionalUser.get().getStatus())) {
            return new TokenValidationResponse(false, null, Collections.emptyList(), Collections.emptyList(), null);
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
