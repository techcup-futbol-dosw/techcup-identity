package edu.eci.dosw.service;

import edu.eci.dosw.dto.AccountResponse;
import edu.eci.dosw.dto.RegisterAccountRequest;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.entity.*;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.exception.*;
import edu.eci.dosw.mapper.*;
import edu.eci.dosw.model.*;
import edu.eci.dosw.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


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
        this.roleMapper = roleMapper;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccountResponse register(RegisterAccountRequest request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Account registration failed: email already exists email={}", request.getEmail());
            throw new EmailAlreadyRegisteredException(request.getEmail());
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

        // TODO:
        // Validate with the corresponding team/tournament domain
        // whether this account can be deactivated.

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
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return accountMapper.toModel(accountEntity);
    }

    private Role findRoleByNameOrThrow(String roleName) {
        RoleEntity roleEntity = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));

        return roleMapper.toModel(roleEntity);
    }

    private void validateRegistrationRules(RegisterAccountRequest request) {
        validateEmailByRelation(request.getEmail(), request.getRelation());

        if (request.getRelation() == null) {
            throw new InvalidRegistrationDataException("Relation is required");
        }

        if (request.getRelation() == Relation.ESTUDIANTE && request.getSemester() == null) {
            throw new MissingRequiredFieldException("Semester", "students");
        }
    }

    private void validateEmailByRelation(String email, Relation relation) {
        if (relation == null || email == null) {
            throw new InvalidRegistrationDataException();
        }

        String normalizedEmail = email.toLowerCase();

        if (relation == Relation.FAMILIAR) {
            if (!normalizedEmail.endsWith("@gmail.com")) {
                throw new InvalidEmailForRelationException("Family accounts must use a Gmail address");
            }
            return;
        }

        if (!normalizedEmail.endsWith("@escuelaing.edu.co")
                && !normalizedEmail.endsWith("@mail.escuelaing.edu.co")) {
            throw new InvalidEmailForRelationException("Institutional relation requires institutional email");
        }
    }

    private Account buildNewAccount(RegisterAccountRequest request, Role playerRole) {
        LocalDateTime now = LocalDateTime.now();

        return new AccountBuilder()
                .name(request.getName())
                .lastName(request.getLastName())
                .birthDate(request.getBirthDate())
                .relation(request.getRelation())
                .semester(request.getSemester())
                .program(request.getProgram().name())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .identificationType(request.getIdentificationType())
                .identification(request.getIdentification())
                .status(AccountStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .addRole(playerRole)
                .build();
    }
}
