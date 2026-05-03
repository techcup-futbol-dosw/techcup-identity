package edu.eci.dosw.security;

import edu.eci.dosw.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    public JwtAuthenticationFilter(JwtService jwtService){
        this.jwtService = jwtService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        // 1. Validar si existe el header y si empieza con Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 2. Extraer token
        String token = authHeader.substring(7);
        try {
            // 3. Validar token
            if (!jwtService.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }
            // 4. Evitar reautenticar si ya existe autenticación en el contexto
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // 5. Extraer claims principales del token
                String userId = jwtService.extractAccountId(token);
                List<String> roles = jwtService.extractRoles(token);
                List<String> permissions = jwtService.extractPermissions(token);
                // 6. Convertir roles y permisos en authorities
                Collection<GrantedAuthority> authorities = buildAuthorities(roles,permissions);
                // 7. Crear Authentication y registrarlo en el contexto
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception ex) {
            // Si el token falla por cualquier razón, limpiar contexto y dejar seguir.
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private Collection<GrantedAuthority> buildAuthorities(List<String> roles, List<String> permissions) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (roles != null) {
            roles.forEach(role ->
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role))
            );
        }

        if (permissions != null) {
            permissions.forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission))
            );
        }

        return authorities;
    }

}
