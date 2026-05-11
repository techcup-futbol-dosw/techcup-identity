package edu.eci.dosw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.eci.dosw.dto.*;
import edu.eci.dosw.service.*;
import edu.eci.dosw.model.*;

@RestController
@RequestMapping("/roles")
@Tag(name = "Roles", description = "Endpoints para asignación, remoción y consulta de roles y permisos")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(
            summary = "Asignar un rol a una cuenta",
            description = "Asigna un rol específico a la cuenta indicada en la solicitud. " +
                    "Solo puede ejecutarse con permisos de administración de roles."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rol asignado correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para asignar roles"),
            @ApiResponse(responseCode = "404", description = "Cuenta o rol no encontrado")
    })
    @PreAuthorize("hasAuthority('role:assign:any')")
    @PostMapping("/assign")
    public ResponseEntity<Void> assignRole(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Cuenta y rol que se desea asignar",
                    required = true
            )
            @RequestBody AssignRoleRequest request
    ) {
        roleService.assignRole(request.getAccountId(), request.getRoleName());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Remover un rol de una cuenta",
            description = "Remueve un rol específico de la cuenta indicada en la solicitud. " +
                    "Solo puede ejecutarse con permisos de administración de roles."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rol removido correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para remover roles"),
            @ApiResponse(responseCode = "404", description = "Cuenta o rol no encontrado")
    })
    @PreAuthorize("hasAuthority('role:remove:any')")
    @PostMapping("/remove")
    public ResponseEntity<Void> removeRole(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Cuenta y rol que se desea remover",
                    required = true
            )
            @RequestBody RemoveRoleRequest request
    ) {
        roleService.removeRole(request.getAccountId(), request.getRoleName());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Consultar roles de una cuenta",
            description = "Obtiene la lista de roles asignados a una cuenta específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles obtenidos correctamente"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para consultar roles"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PreAuthorize("hasAuthority('role:read:any')")
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Role>> getRolesByAccount(
            @Parameter(description = "Identificador único de la cuenta", required = true)
            @PathVariable Long accountId
    ) {
        return ResponseEntity.ok(roleService.getRolesByAccount(accountId));
    }

    @Operation(
            summary = "Consultar permisos de un rol",
            description = "Obtiene la lista de permisos asociados a un rol específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permisos obtenidos correctamente"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para consultar permisos"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    @PreAuthorize("hasAuthority('permission:read:any')")
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<List<Permission>> getPermissionsByRole(
            @Parameter(description = "Identificador único del rol", required = true)
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(roleService.getPermissions(roleId));
    }
}