package edu.eci.dosw.service;

import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.model.Role;
import edu.eci.dosw.repository.AccountRepository;
import edu.eci.dosw.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import edu.eci.dosw.mapper.*;
import edu.eci.dosw.dto.*;

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
                          PasswordEncoder passwordEncoder){
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleMapper = roleMapper;
    }

    @Transactional
    public Account register(RegisterAccountRequest request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Account registration failed: email already exists email={}", request.getEmail());
            throw new RuntimeException("Email already registered");
        }
        validateRegistrationRules(request);
        Role playerRole = findRoleByNameOrThrow("PLAYER");
        Account account = buildNewAccount(request, playerRole);
        AccountEntity savedEntity = accountRepository.save(accountMapper.toEntity(account));
        log.info("Account registered successfully for email={}", request.getEmail());
        return accountMapper.toModel(savedEntity);
    }


    @Transactional(readOnly = true)
    public Account findById(Long accountId) {
        return findAccountByIdOrThrow(accountId);
    }

    @Transactional
    public void deactivate(Long accountId) {
        //falta implementar
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
        RoleEntity roleEntity = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return roleMapper.toModel(roleEntity);
    }


    private void validateRegistrationRules(RegisterAccountRequest request) {
        validateEmailByRelation(request.getEmail(), request.getRelation());

        //if ("student".equalsIgnoreCase(request.getRelation())) {
        //    if (request.getSemester() == null) {
        //        throw new RuntimeException("Semester is required for students");
        //    }
        //}

        // validar el resto de atributos si es necesario??
    }

    private void validateEmailByRelation(String email, String relation) {
        if (relation == null || email == null) {
            throw new RuntimeException("Invalid registration data");
        }

        if ("family".equalsIgnoreCase(relation)) {
            if (!email.toLowerCase().endsWith("@gmail.com")) {
                throw new RuntimeException("Family accounts must use a Gmail address");
            }
            return;
        }

        if (!email.toLowerCase().endsWith("@escuelaing.edu.co") && !email.toLowerCase().endsWith("@mail.escuelaing.edu.co")) {
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