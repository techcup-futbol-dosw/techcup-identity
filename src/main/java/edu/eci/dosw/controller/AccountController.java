package edu.eci.dosw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.eci.dosw.dto.*;
import edu.eci.dosw.service.*;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Cuentas", description = "Endpoints para registro, consulta y gestión de cuentas")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @Operation(
            summary = "Registrar una nueva cuenta",
            description = "Crea una nueva cuenta en el sistema con la información enviada en la solicitud."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuenta registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud"),
            @ApiResponse(responseCode = "409", description = "El correo ya se encuentra registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Información necesaria para registrar la cuenta",
                    required = true
            )
            @RequestBody RegisterAccountRequest request
    ){
        AccountResponse response = accountService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Consultar una cuenta por ID",
            description = "Obtiene la información de una cuenta a partir de su identificador único. " +
                    "El acceso está restringido a usuarios con permiso global o al propietario de la cuenta."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para consultar esta cuenta"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PreAuthorize("hasAuthority('account:read:any') or @accountAccessPolicy.canReadAccount(#accountId, authentication)")
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getById(
            @Parameter(description = "Identificador único de la cuenta", required = true)
            @PathVariable Long accountId
    ) {
        AccountResponse response = accountService.findById(accountId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Consultar una cuenta por correo electrónico",
            description = "Obtiene la información de una cuenta a partir de su correo electrónico. " +
                    "El acceso está restringido a usuarios con permiso global o al propietario de la cuenta."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para consultar esta cuenta"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PreAuthorize("hasAuthority('account:read:any') or @accountAccessPolicy.canReadAccount(#accountEmail, authentication)")
    @GetMapping("/email/{accountEmail}")
    public ResponseEntity<AccountResponse> getByEmail(
            @Parameter(description = "Correo electrónico asociado a la cuenta", required = true)
            @PathVariable String accountEmail
    ) {
        AccountResponse response = accountService.findByEmail(accountEmail);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Desactivar una cuenta",
            description = "Marca una cuenta como inactiva. Solo puede ser ejecutado por usuarios con permiso de desactivación global."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cuenta desactivada correctamente"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para desactivar cuentas"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PreAuthorize("hasAuthority('account:deactivate:any')")
    @PatchMapping("/{accountId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "Identificador único de la cuenta a desactivar", required = true)
            @PathVariable Long accountId
    ) {
        accountService.deactivate(accountId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Verificar si un correo ya existe",
            description = "Indica si ya existe una cuenta registrada con el correo electrónico proporcionado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validación realizada correctamente")
    })
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByEmail(
            @Parameter(description = "Correo electrónico a verificar", required = true)
            @RequestParam String email
    ) {
        return ResponseEntity.ok(accountService.existsByEmail(email));
    }
}