package edu.eci.dosw.service;

import edu.eci.dosw.dto.AccountResponse;
import edu.eci.dosw.dto.RegisterAccountRequest;
import edu.eci.dosw.dto.Relation;
import edu.eci.dosw.entity.*;
import edu.eci.dosw.entity.AccountStatus;
import edu.eci.dosw.mapper.*;
import edu.eci.dosw.model.*;
import edu.eci.dosw.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository,
                          RoleRepository roleRepository,
                          RoleMapper roleMapper,
                          AccountMapper accountMapper,
                          PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleMapper = roleMapper;
    }

    @Transactional
    public AccountResponse register(RegisterAccountRequest request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Account registration failed: email already exists email={}", request.getEmail());
            throw new RuntimeException("Email already registered");
        }
        validateRegistrationRules(request);
        Role playerRole = findRoleByNameOrThrow("PLAYER");
        Account account = buildNewAccount(request, playerRole);
        AccountEntity savedEntity = accountRepository.save(accountMapper.toEntity(account));
        log.info("Account registered successfully for email={}", request.getEmail());
        return accountMapper.toResponse(accountMapper.toModel(savedEntity));
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(Long accountId) {
        Account account = findAccountByIdOrThrow(accountId);
        return accountMapper.toResponse(account);
    }

    @Transactional
    public void deactivate(Long accountId) {
        Account account = findAccountByIdOrThrow(accountId);
        if (!account.isActive()) {
            log.warn("Deactivation skipped: account already inactive accountId={}", accountId);
            return;
        }
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(accountMapper.toEntity(account));
        log.info("Account deactivated successfully accountId={}", accountId);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return accountRepository.findByEmail(email).isPresent();
    }

    private Account findAccountByIdOrThrow(Long accountId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return accountMapper.toModel(accountEntity);
    }

    private Role findRoleByNameOrThrow(String roleName) {
        RoleEntity roleEntity = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return roleMapper.toModel(roleEntity);
    }

    private void validateRegistrationRules(RegisterAccountRequest request) {
        validateEmailByRelation(request.getEmail(), request.getRelation());

        if (request.getRelation() == Relation.ESTUDIANTE) {
            if (request.getSemester() == null) {
                throw new RuntimeException("Semester is required for students");
            }
        }
    }

    private void validateEmailByRelation(String email, Relation relation) {
        if (relation == null || email == null) {
            throw new RuntimeException("Invalid registration data");
        }

        if (relation == Relation.FAMILIAR) {
            if (email == null || !email.toLowerCase().endsWith("@gmail.com")) {
                throw new RuntimeException("Family accounts must use a Gmail address");
            }
            return;
        }

        if (!email.toLowerCase().endsWith("@escuelaing.edu.co")
                && !email.toLowerCase().endsWith("@mail.escuelaing.edu.co")) {
            throw new RuntimeException("Institutional relation requires institutional email");
        }
    }

    private Account buildNewAccount(RegisterAccountRequest request, Role playerRole) {
        LocalDateTime now = LocalDateTime.now();

        return new AccountBuilder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdAt(now)
                .addRole(playerRole)
                .build();
    }
}