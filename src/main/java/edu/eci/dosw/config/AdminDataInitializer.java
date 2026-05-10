package edu.eci.dosw.config;

import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.exception.RoleNotFoundException;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.model.Gender;
import edu.eci.dosw.model.IdentificationType;
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

import java.time.LocalDate;
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
            @Value("${app.seed.admin.password}") String adminPassword,

            @Value("${app.seed.admin.name:Admin}") String adminName,
            @Value("${app.seed.admin.last-name:System}") String adminLastName,
            @Value("${app.seed.admin.birth-date:2000-01-01}") String adminBirthDate,
            @Value("${app.seed.admin.relation:ESTUDIANTE}") Relation adminRelation,
            @Value("${app.seed.admin.semester:1}") Integer adminSemester,
            @Value("${app.seed.admin.program:SISTEMAS}") String adminProgram,
            @Value("${app.seed.admin.gender:MALE}") Gender adminGender,
            @Value("${app.seed.admin.identification-type:CC}") IdentificationType adminIdentificationType,
            @Value("${app.seed.admin.identification:ADMIN-0001}") String adminIdentification
    ) {
        return args -> {
            if (accountRepository.findByEmail(adminEmail).isPresent()) {
                log.info("Admin seed skipped: admin account already exists");
                return;
            }

            RoleEntity adminRoleEntity = roleRepository.findByNameIgnoreCase("ADMIN")
                    .orElseThrow(() -> new RoleNotFoundException("ADMIN"));

            adminRoleEntity.setPermissions(permissionRepository.findAll());
            roleRepository.save(adminRoleEntity);

            Role adminRole = new Role(
                    adminRoleEntity.getId(),
                    adminRoleEntity.getName(),
                    Collections.emptyList()
            );

            LocalDateTime now = LocalDateTime.now();

            Account adminAccount = new AccountBuilder()
                    .name(adminName)
                    .lastName(adminLastName)
                    .birthDate(LocalDate.parse(adminBirthDate))
                    .relation(adminRelation)
                    .semester(adminSemester)
                    .program(adminProgram)
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .status(AccountStatus.ACTIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .gender(adminGender)
                    .identificationType(adminIdentificationType)
                    .identification(adminIdentification)
                    .addRole(adminRole)
                    .build();

            accountRepository.save(accountMapper.toEntity(adminAccount));

            log.info("Admin account seeded successfully with email={}", adminEmail);
        };
    }
}
