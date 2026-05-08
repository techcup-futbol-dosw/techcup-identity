package edu.eci.dosw.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.eci.dosw.dto.*;
import edu.eci.dosw.service.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }
    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(@Valid @RequestBody RegisterAccountRequest request){
        AccountResponse response = accountService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('account:read:any') or @accountAccessPolicy.canReadAccount(#accountId, authentication)")
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getById(@PathVariable Long accountId) {
        AccountResponse response = accountService.findById(accountId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('account:deactivate:any')")
    @PatchMapping("/{accountId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long accountId) {
        accountService.deactivate(accountId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(accountService.existsByEmail(email));
    }


}