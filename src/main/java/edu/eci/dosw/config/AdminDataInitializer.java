package edu.eci.dosw.config;


import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.entity.AccountStatus;
import edu.eci.dosw.model.Role;
import edu.eci.dosw.repository.AccountRepository;
import edu.eci.dosw.repository.PermissionRepository;
import edu.eci.dosw.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;

@Configuration
public class AdminDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    @Bean
    CommandLineRunner initAdminAccount(
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            AccountMapper accountMapper,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin.email}") String adminEmail,
            @Value("${app.seed.admin.password}") String adminPassword
    ) {
        return args -> {
            if (accountRepository.findByEmail(adminEmail).isPresent()) {
                log.info("Admin seed skipped: admin account already exists");
                return;
            }

            RoleEntity adminRoleEntity = roleRepository.findByNameIgnoreCase("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            adminRoleEntity.setPermissions(permissionRepository.findAll());
            roleRepository.save(adminRoleEntity);

            Role adminRole = new Role(
                    adminRoleEntity.getId(),
                    adminRoleEntity.getName(),
                    Collections.emptyList()
            );

            LocalDateTime now = LocalDateTime.now();

            Account adminAccount = new AccountBuilder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .status(AccountStatus.ACTIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .addRole(adminRole)
                    .build();

            accountRepository.save(accountMapper.toEntity(adminAccount));

            log.info("Admin account seeded successfully with email={}", adminEmail);
        };
    }
}
