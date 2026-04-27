package edu.eci.dosw.service;

import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
public class JwtService {
    private static final String ROLES_CLAIM = "roles";
    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${security.jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(Long userId, List<String> roles, List<String> permissions) {
        Map<String, Object> claims = Map.of(
                ROLES_CLAIM, roles != null ? roles : Collections.emptyList(),
                PERMISSIONS_CLAIM, permissions != null ? permissions : Collections.emptyList(),
                TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE
        );

        return buildToken(claims, String.valueOf(userId), accessTokenExpiration);
    }

    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = Map.of(
                TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE
        );

        return buildToken(claims, String.valueOf(userId), refreshTokenExpiration);
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get(ROLES_CLAIM);
        if (rolesObj instanceof List<?> rawList) {
            List<String> roles = new ArrayList<>();
            for (Object item : rawList) {
                roles.add(String.valueOf(item));
            }
            return roles;
        }
        return Collections.emptyList();
    }

    public List<String> extractPermissions(String token) {
        Claims claims = extractAllClaims(token);
        Object permissionsObj = claims.get(PERMISSIONS_CLAIM);

        if (permissionsObj instanceof List<?> rawList) {
            List<String> permissions = new ArrayList<>();
            for (Object item : rawList) {
                permissions.add(String.valueOf(item));
            }
            return permissions;
        }

        return Collections.emptyList();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);

            return ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
                    && !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);

            return REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
                    && !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    public String extractTokenType(String token) {
        return extractAllClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
