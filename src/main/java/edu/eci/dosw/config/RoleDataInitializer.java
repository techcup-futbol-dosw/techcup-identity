package edu.eci.dosw.config;

import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.repository.PermissionRepository;
import edu.eci.dosw.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RoleDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(RoleDataInitializer.class);

    @Bean
    @Order(2)
    CommandLineRunner roleInitializer(RoleRepository roleRepository,
                                      PermissionRepository permissionRepository) {
        return args -> {
            createOrUpdateRole(roleRepository, permissionRepository, "PLAYER", List.of(
                    "account:read:self",
                    "session:refresh:self",
                    "session:logout:self",
                    "user:read:self",
                    "user:update:self",
                    "sport-profile:create:self",
                    "sport-profile:read:self",
                    "sport-profile:update:self",
                    "invitation:read:self",
                    "invitation:respond:self",
                    "standings:read:any",
                    "bracket:read:any",
                    "stats:read:any",
                    "top-scorer:read:any",
                    "history:read:any",
                    "result:read:any",
                    "regulation:read:any"
            ));

            createOrUpdateRole(roleRepository, permissionRepository, "CAPTAIN", List.of(
                    "account:read:self",
                    "session:refresh:self",
                    "session:logout:self",
                    "user:read:self",
                    "user:update:self",
                    "sport-profile:create:self",
                    "sport-profile:read:self",
                    "sport-profile:update:self",
                    "invitation:read:self",
                    "invitation:respond:self",
                    "standings:read:any",
                    "bracket:read:any",
                    "stats:read:any",
                    "top-scorer:read:any",
                    "history:read:any",
                    "result:read:any",
                    "regulation:read:any",

                    "player:search:any",
                    "player:read:any",
                    "team:create:own",
                    "team:read:own",
                    "team:update:own",
                    "team:delete:own",
                    "team-member:add:own",
                    "team-member:remove:own",
                    "team-member:read:own",
                    "team-invitation:create:own",
                    "team-invitation:read:own",
                    "team-invitation:manage:own",
                    "lineup:create:own",
                    "lineup:read:own",
                    "lineup:update:own",
                    "lineup:read:team",
                    "registration:create:own",
                    "registration:read:own",
                    "registration:cancel:own"
            ));

            createOrUpdateRole(roleRepository, permissionRepository, "ORGANIZER", List.of(
                    "tournament:create:any",
                    "tournament:read:any",
                    "tournament:update:any",
                    "tournament:activate:any",
                    "tournament:start:any",
                    "tournament:finish:any",
                    "tournament:delete:any",
                    "registration:read:any",
                    "registration:review:any",
                    "registration:approve:any",
                    "registration:reject:any",
                    "regulation:read:any",
                    "regulation:update:any",
                    "venue:create:any",
                    "venue:read:any",
                    "venue:update:any",
                    "venue:delete:any",
                    "match:create:any",
                    "match:read:any",
                    "match:update:any",
                    "match:delete:any",
                    "referee-assignment:update:any",
                    "referee-assignment:read:any",
                    "result:register:any",
                    "result:read:any",
                    "goal:register:any",
                    "card:register:any",
                    "standings:read:any",
                    "bracket:read:any",
                    "stats:read:any",
                    "top-scorer:read:any",
                    "history:read:any"
            ));

            createOrUpdateRole(roleRepository, permissionRepository, "REFEREE", List.of(
                    "referee-match:read:assigned"
            ));

            // ADMIN gets all permissions
            RoleEntity adminRole = roleRepository.findByNameIgnoreCase("ADMIN")
                    .orElseGet(() -> {
                        RoleEntity role = new RoleEntity();
                        role.setName("ADMIN");
                        return role;
                    });

            adminRole.setPermissions(new ArrayList<>(permissionRepository.findAll()));
            roleRepository.save(adminRole);

            log.info("Role data initialization completed");
        };
    }

    private void createOrUpdateRole(RoleRepository roleRepository,
                                    PermissionRepository permissionRepository,
                                    String roleName,
                                    List<String> permissionNames) {

        RoleEntity role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseGet(() -> {
                    RoleEntity newRole = new RoleEntity();
                    newRole.setName(roleName);
                    return newRole;
                });

        List<PermissionEntity> permissions = new ArrayList<>();

        for (String permissionName : permissionNames) {
            permissionRepository.findByNameIgnoreCase(permissionName)
                    .ifPresent(permission -> {
                        boolean alreadyAdded = permissions.stream()
                                .anyMatch(existing -> existing.getName().equalsIgnoreCase(permission.getName()));

                        if (!alreadyAdded) {
                            permissions.add(permission);
                        }
                    });
        }

        role.setPermissions(permissions);
        roleRepository.save(role);

        log.info("Role created/updated: {}", roleName);
    }
}