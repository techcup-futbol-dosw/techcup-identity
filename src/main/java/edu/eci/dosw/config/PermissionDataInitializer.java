package edu.eci.dosw.config;

import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.repository.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class PermissionDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(PermissionDataInitializer.class);

    private static final List<String> PERMISSIONS = List.of(
            // 1) Identity / account / authentication
            "account:create:any",
            "account:read:self",
            "account:read:any",
            "account:update:self",
            "account:update:any",
            "account:deactivate:any",
            "account:exists:any",

            // 2) Session / tokens
            "session:login:any",
            "session:refresh:self",
            "session:logout:self",
            "session:validate:any",

            // 3) Roles and permissions
            "role:assign:any",
            "role:remove:any",
            "role:read:any",
            "permission:read:any",

            // 4) Basic user / profile
            "user:read:self",
            "user:read:any",
            "user:update:self",
            "user:update:any",

            // 5) Sport profile
            "sport-profile:create:self",
            "sport-profile:read:self",
            "sport-profile:read:any",
            "sport-profile:update:self",

            // 6) Players / search
            "player:search:any",
            "player:read:any",

            // 7) Invitations
            "invitation:read:self",
            "invitation:respond:self",
            "invitation:create:own",
            "invitation:manage:own",

            // 8) Teams
            "team:create:own",
            "team:read:own",
            "team:read:any",
            "team:update:own",
            "team:update:any",
            "team:delete:own",
            "team:delete:any",

            // 9) Team members
            "team-member:add:own",
            "team-member:remove:own",
            "team-member:read:own",
            "team-member:read:any",

            // 10) Team invitations
            "team-invitation:create:own",
            "team-invitation:read:own",
            "team-invitation:manage:own",

            // 11) Tournaments
            "tournament:create:any",
            "tournament:read:any",
            "tournament:update:any",
            "tournament:activate:any",
            "tournament:start:any",
            "tournament:finish:any",
            "tournament:delete:any",

            // 12) Tournament registrations
            "registration:create:own",
            "registration:read:own",
            "registration:read:any",
            "registration:cancel:own",
            "registration:review:any",
            "registration:approve:any",
            "registration:reject:any",

            // 13) Regulations and venues
            "regulation:read:any",
            "regulation:update:any",
            "venue:create:any",
            "venue:read:any",
            "venue:update:any",
            "venue:delete:any",

            // 14) Standings / brackets / stats
            "standings:read:any",
            "bracket:read:any",
            "stats:read:any",
            "top-scorer:read:any",
            "history:read:any",

            // 15) Matches
            "match:create:any",
            "match:read:any",
            "match:update:any",
            "match:delete:any",

            // 16) Referee assignment
            "referee-assignment:update:any",
            "referee-assignment:read:any",

            // 17) Lineups
            "lineup:create:own",
            "lineup:read:own",
            "lineup:update:own",
            "lineup:read:team",

            // 18) Results and match events
            "result:register:any",
            "result:read:any",
            "goal:register:any",
            "card:register:any",

            // 19) Referee queries
            "referee-match:read:assigned"
    );

    @Bean
    @Order(1)
    CommandLineRunner permissionInitializer(PermissionRepository permissionRepository) {
        return args -> {
            for (String permissionName : PERMISSIONS) {
                boolean exists = permissionRepository.findByNameIgnoreCase(permissionName).isPresent();

                if (!exists) {
                    PermissionEntity permission = new PermissionEntity();
                    permission.setName(permissionName);
                    permissionRepository.save(permission);
                    log.info("Permission created: {}", permissionName);
                }
            }

            log.info("Permission data initialization completed");
        };
    }
}