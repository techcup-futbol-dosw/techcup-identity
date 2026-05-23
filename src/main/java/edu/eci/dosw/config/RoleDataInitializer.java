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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class RoleDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(RoleDataInitializer.class);

    private static final Set<String> PLAYER_PERMISSIONS = Set.of(
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
    );

    private static final Set<String> CAPTAIN_EXTRA_PERMISSIONS = Set.of(
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
    );

    private static final Set<String> ORGANIZER_PERMISSIONS = Set.of(
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
            "goal:update:any",
            "card:register:any",
            "card:update:any",
            "standings:read:any",
            "bracket:read:any",
            "stats:read:any",
            "top-scorer:read:any",
            "history:read:any"
    );

    private static final Set<String> REFEREE_PERMISSIONS = Set.of(
            "referee-match:read:assigned",
            "result:read:assigned",
            "goal:register:assigned",
            "goal:update:assigned",
            "card:register:assigned",
            "card:update:assigned",
            "match:update:any"
    );

    @Bean
    @Order(2)
    public CommandLineRunner roleInitializer(RoleRepository roleRepository,
                                             PermissionRepository permissionRepository) {
        return args -> {
            Map<String, PermissionEntity> permissionMap = permissionRepository.findAll().stream()
                    .collect(Collectors.toMap(
                            permission -> permission.getName().toLowerCase(Locale.ROOT),
                            permission -> permission
                    ));

            createOrUpdateRole(
                    roleRepository,
                    permissionMap,
                    "PLAYER",
                    PLAYER_PERMISSIONS
            );

            createOrUpdateRole(
                    roleRepository,
                    permissionMap,
                    "CAPTAIN",
                    mergePermissions(PLAYER_PERMISSIONS, CAPTAIN_EXTRA_PERMISSIONS)
            );

            createOrUpdateRole(
                    roleRepository,
                    permissionMap,
                    "ORGANIZER",
                    ORGANIZER_PERMISSIONS
            );

            createOrUpdateRole(
                    roleRepository,
                    permissionMap,
                    "REFEREE",
                    REFEREE_PERMISSIONS
            );

            RoleEntity adminRole = roleRepository.findByNameIgnoreCase("ADMIN")
                    .orElseGet(newRole("ADMIN"));

            adminRole.setPermissions(new ArrayList<>(permissionMap.values()));
            roleRepository.save(adminRole);

            log.info("Role data initialization completed");
        };
    }

    private void createOrUpdateRole(RoleRepository roleRepository,
                                    Map<String, PermissionEntity> permissionMap,
                                    String roleName,
                                    Set<String> permissionNames) {

        RoleEntity role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseGet(newRole(roleName));

        List<PermissionEntity> permissions = permissionNames.stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .map(permissionMap::get)
                .filter(java.util.Objects::nonNull)
                .toList();

        role.setPermissions(new ArrayList<>(permissions));
        roleRepository.save(role);

        log.info("Role created/updated: {}", roleName);
    }

    private Supplier<RoleEntity> newRole(String roleName) {
        return () -> {
            RoleEntity role = new RoleEntity();
            role.setName(roleName);
            return role;
        };
    }

    @SafeVarargs
    private final Set<String> mergePermissions(Set<String>... groups) {
        return Stream.of(groups)
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}