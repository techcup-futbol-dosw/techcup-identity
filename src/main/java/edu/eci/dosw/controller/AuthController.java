package edu.eci.dosw.controller;

import edu.eci.dosw.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.dosw.dto.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para autenticación, renovación, cierre y validación de tokens")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica al usuario con correo y contraseña, y retorna un access token y un refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas o cuenta inactiva")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales del usuario",
                    required = true
            )
            @RequestBody AuthRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Renovar tokens",
            description = "Genera un nuevo access token y un nuevo refresh token a partir de un refresh token válido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens renovados correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado o revocado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token que será validado para emitir nuevos tokens",
                    required = true
            )
            @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cerrar sesión",
            description = "Revoca el refresh token enviado en la solicitud, invalidando la sesión asociada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sesión cerrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Refresh token no encontrado")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token que será revocado",
                    required = true
            )
            @RequestBody LogoutRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Validar un token",
            description = "Valida un token JWT y retorna información sobre su validez, tipo, roles y permisos asociados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validación realizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Token JWT a validar",
                    required = true
            )
            @RequestBody TokenValidationRequest request
    ) {
        TokenValidationResponse response = authService.validateToken(request);
        return ResponseEntity.ok(response);
    }
}