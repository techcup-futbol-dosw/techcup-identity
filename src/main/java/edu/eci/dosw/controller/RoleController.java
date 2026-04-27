package edu.eci.dosw.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PreAuthorize("hasAuthority('role:assign')")
    @PostMapping("/assign")
    public ResponseEntity<Void> assignRole(@Valid @RequestBody AssignRoleRequest request) {
        roleService.assignRole(request.getAccountId(), request.getRoleName());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('role:remove')")
    @PostMapping("/remove")
    public ResponseEntity<Void> removeRole(@Valid @RequestBody RemoveRoleRequest request) {
        roleService.removeRole(request.getAccountId(), request.getRoleName());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('role:read:any')")
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Role>> getRolesByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(roleService.getRolesByAccount(accountId));
    }

    @PreAuthorize("hasAuthority('permission:read:any')")
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<List<Permission>> getPermissionsByRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.getPermissions(roleId));
    }
}
