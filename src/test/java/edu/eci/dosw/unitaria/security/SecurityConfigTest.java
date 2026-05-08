package edu.eci.dosw.unitaria.security;

import edu.eci.dosw.security.AccessDeniedHandlerImpl;
import edu.eci.dosw.security.AuthenticationEntryPointImpl;
import edu.eci.dosw.security.JwtAuthenticationFilter;
import edu.eci.dosw.security.SecurityConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationEntryPointImpl authenticationEntryPoint;

    @Mock
    private AccessDeniedHandlerImpl accessDeniedHandler;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpSecurity httpSecurity;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(
                jwtAuthenticationFilter,
                authenticationEntryPoint,
                accessDeniedHandler
        );
    }

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {

        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    void authenticationManager_ShouldReturnAuthenticationManager() throws Exception {

        when(authenticationConfiguration.getAuthenticationManager())
                .thenReturn(authenticationManager);

        AuthenticationManager result =
                securityConfig.authenticationManager(authenticationConfiguration);

        assertNotNull(result);
        assertEquals(authenticationManager, result);

        verify(authenticationConfiguration)
                .getAuthenticationManager();
    }

    @Test
    void passwordEncoder_ShouldEncodePasswordCorrectly() {

        PasswordEncoder encoder = securityConfig.passwordEncoder();

        String rawPassword = "123456";

        String encodedPassword =
                encoder.encode(rawPassword);

        assertNotNull(encodedPassword);

        assertNotEquals(rawPassword, encodedPassword);

        assertTrue(
                encoder.matches(rawPassword, encodedPassword)
        );
    }

    @Test
    void filterChain_ShouldConfigureSecurityCorrectly() throws Exception {
        HttpSecurity httpSecurity =
                mock(HttpSecurity.class, RETURNS_SELF);
        DefaultSecurityFilterChain securityFilterChain =
                mock(DefaultSecurityFilterChain.class);
        when(httpSecurity.build())
                .thenReturn(securityFilterChain);

        SecurityFilterChain result =
                securityConfig.filterChain(httpSecurity);
        assertNotNull(result);
        verify(httpSecurity).csrf(any());
        verify(httpSecurity).cors(any());
        verify(httpSecurity).sessionManagement(any());
        verify(httpSecurity).exceptionHandling(any());
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).addFilterBefore(
                eq(jwtAuthenticationFilter),
                eq(UsernamePasswordAuthenticationFilter.class)
        );
        verify(httpSecurity).build();
    }
}