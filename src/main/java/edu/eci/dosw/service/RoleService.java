package edu.eci.dosw.service;

import java.util.Collections;
import java.util.List;

import edu.eci.dosw.exception.AccountNotFoundException;
import edu.eci.dosw.exception.RoleNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.eci.dosw.mapper.*;
import edu.eci.dosw.repository.*;
import edu.eci.dosw.model.*;
import edu.eci.dosw.entity.*;


@Service
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final RoleMapper roleMapper;
    private final AccountMapper accountMapper;

    public RoleService(RoleRepository roleRepository,
                       AccountRepository accountRepository,
                       RoleMapper roleMapper,
                       AccountMapper accountMapper) {
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.roleMapper = roleMapper;
        this.accountMapper = accountMapper;
    }

    @Transactional
    public void assignRole(Long accountId, String roleName) {
        Account account = findAccountByIdOrThrow(accountId);
        Role role = findRoleByNameOrThrow(roleName);

        boolean alreadyAssigned = account.getRoles() != null &&
                account.getRoles().stream().anyMatch(r -> r.getId().equals(role.getId()));

        if (alreadyAssigned) {
            log.warn("Role assignment skipped: role={} already assigned to accountId={}", roleName, accountId);
            return;
        }

        account.addRole(role);
        saveAccount(account);

        log.info("Role assigned successfully: role={} accountId={}", roleName, accountId);
    }

    @Transactional
    public void removeRole(Long accountId, String roleName) {
        Account account = findAccountByIdOrThrow(accountId);
        Role role = findRoleByNameOrThrow(roleName);

        boolean assigned = account.getRoles() != null &&
                account.getRoles().stream().anyMatch(r -> r.getId().equals(role.getId()));

        if (!assigned) {
            log.warn("Role removal skipped: role={} not assigned to accountId={}", roleName, accountId);
            return;
        }

        account.removeRole(role);
        saveAccount(account);

        log.info("Role removed successfully: role={} accountId={}", roleName, accountId);
    }

    @Transactional(readOnly = true)
    public List<Role> getRolesByAccount(Long accountId) {
        Account account = findAccountByIdOrThrow(accountId);

        if (account.getRoles() == null) {
            return Collections.emptyList();
        }

        return account.getRoles();
    }

    @Transactional(readOnly = true)
    public List<Permission> getPermissions(Long roleId) {
        Role role = findRoleByIdOrThrow(roleId);

        if (role.getPermissions() == null) {
            return Collections.emptyList();
        }

        return role.getPermissions();
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(Long roleId, String permissionName) {
        return getPermissions(roleId).stream()
                .anyMatch(permission -> permission.getName().equalsIgnoreCase(permissionName));
    }

    private Account findAccountByIdOrThrow(Long accountId) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return accountMapper.toModel(accountEntity);
    }

    private Role findRoleByIdOrThrow(Long roleId) {
        RoleEntity roleEntity = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("id=" + roleId));

        return roleMapper.toModel(roleEntity);
    }

    private Role findRoleByNameOrThrow(String roleName) {
        RoleEntity roleEntity = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));

        return roleMapper.toModel(roleEntity);
    }

    private void saveAccount(Account account) {
        accountRepository.save(accountMapper.toEntity(account));
    }
}

