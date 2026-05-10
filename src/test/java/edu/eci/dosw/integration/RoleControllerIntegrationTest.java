package edu.eci.dosw.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.dto.AssignRoleRequest;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.dto.RemoveRoleRequest;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.*;
import edu.eci.dosw.repository.AccountRepository;
import edu.eci.dosw.repository.PermissionRepository;
import edu.eci.dosw.repository.RefreshTokenRepository;
import edu.eci.dosw.repository.RoleRepository;
import edu.eci.dosw.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static edu.eci.dosw.testutil.TestDataFactory.validAccountBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PermissionRepository permissionRepository;


    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private RoleEntity playerRoleEntity;
    private RoleEntity captainRoleEntity;
    private RoleEntity adminRoleEntity;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();
        permissionRepository.deleteAllInBatch();

        playerRoleEntity = createRole("PLAYER");
        captainRoleEntity = createRole("CAPTAIN");
        adminRoleEntity = createRole("ADMIN");
    }

    // =========================================================
    // ASSIGN ROLE
    // =========================================================

    @Test
    @DisplayName("Should assign role successfully when caller has permission")
    void shouldAssignRoleSuccessfully() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "assign@mail.escuelaing.edu.co",
                "Password123*",
                AccountStatus.ACTIVE,
                playerRoleEntity
        );

        String token = jwtService.generateAccessToken(
                999L,
                List.of("ADMIN"),
                List.of("role:assign:any")
        );

        AssignRoleRequest request = new AssignRoleRequest();
        request.setAccountId(accountEntity.getId());
        request.setRoleName("CAPTAIN");

        mockMvc.perform(post("/roles/assign")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        AccountEntity updatedAccount = accountRepository.findById(accountEntity.getId()).orElseThrow();

        assertThat(updatedAccount.getRoles())
                .extracting(RoleEntity::getName)
                .contains("PLAYER", "CAPTAIN");
    }

    @Test
    @DisplayName("Should return 403 when caller tries to assign role without permission")
    void shouldReturnForbiddenWhenAssigningWithoutPermission() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "forbidden-assign@mail.escuelaing.edu.co",
                "Password123*",
                AccountStatus.ACTIVE,
                playerRoleEntity
        );

        String token = jwtService.generateAccessToken(
                999L,
                List.of("PLAYER"),
                List.of("account:read:self")
        );

        AssignRoleRequest request = new AssignRoleRequest();
        request.setAccountId(accountEntity.getId());
        request.setRoleName("CAPTAIN");

        mockMvc.perform(post("/roles/assign")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when assigning role to non-existing account")
    void shouldReturnNotFoundWhenAssigningRoleToMissingAccount() throws Exception {
        String token = jwtService.generateAccessToken(
                999L,
                List.of("ADMIN"),
                List.of("role:assign:any")
        );

        AssignRoleRequest request = new AssignRoleRequest();
        request.setAccountId(99999L);
        request.setRoleName("CAPTAIN");

        mockMvc.perform(post("/roles/assign")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Should return 404 when assigning non-existing role")
    void shouldReturnNotFoundWhenAssigningMissingRole() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "missingrole@mail.escuelaing.edu.co",
                "Password123*",
                AccountStatus.ACTIVE,
                playerRoleEntity
        );

        String token = jwtService.generateAccessToken(
                999L,
                List.of("ADMIN"),
                List.of("role:assign:any")
        );

        AssignRoleRequest request = new AssignRoleRequest();
        request.setAccountId(accountEntity.getId());
        request.setRoleName("UNKNOWN_ROLE");

        mockMvc.perform(post("/roles/assign")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // =========================================================
    // REMOVE ROLE
    // =========================================================

    @Test
    @DisplayName("Should remove role successfully when caller has permission")
    void shouldRemoveRoleSuccessfully() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "remove@mail.escuelaing.edu.co",
                "Password123*",
                AccountStatus.ACTIVE,
                playerRoleEntity,
                captainRoleEntity
        );

        String token = jwtService.generateAccessToken(
                999L,
                List.of("ADMIN"),
                List.of("role:remove:any")
        );

        RemoveRoleRequest request = new RemoveRoleRequest();
        request.setAccountId(accountEntity.getId());
        request.setRoleName("CAPTAIN");

        mockMvc.perform(post("/roles/remove")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        AccountEntity updatedAccount = accountRepository.findById(accountEntity.getId()).orElseThrow();

        assertThat(updatedAccount.getRoles())
                .extracting(RoleEntity::getName)
                .contains("PLAYER")
                .doesNotContain("CAPTAIN");
    }

    // =========================================================
    // GET ROLES BY ACCOUNT
    // =========================================================

    @Test
    @DisplayName("Should return roles by account when caller has read permission")
    void shouldReturnRolesByAccountSuccessfully() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "roles@mail.escuelaing.edu.co",
                "Password123*",
                AccountStatus.ACTIVE,
                playerRoleEntity,
                captainRoleEntity
        );

        String token = jwtService.generateAccessToken(
                999L,
                List.of("ADMIN"),
                List.of("role:read:any")
        );

        mockMvc.perform(get("/roles/account/{accountId}", accountEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[*].name").isArray());
    }

    // =========================================================
    // GET PERMISSIONS BY ROLE
    // =========================================================

    @Test
    @DisplayName("Should return permissions by role when caller has permission")
    void shouldReturnPermissionsByRoleSuccessfully() throws Exception {
        PermissionEntity permission = new PermissionEntity();
        permission.setName("team:create:own");
        permission = permissionRepository.save(permission);

        captainRoleEntity.setPermissions(new ArrayList<>(List.of(permission)));
        captainRoleEntity = roleRepository.save(captainRoleEntity);

        String token = jwtService.generateAccessToken(
                999L,
                List.of("ADMIN"),
                List.of("permission:read:any")
        );

        mockMvc.perform(get("/roles/{roleId}/permissions", captainRoleEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("team:create:own"));
    }
    @Test
    @DisplayName("Should return 403 when caller tries to read permissions without permission")
    void shouldReturnForbiddenWhenReadingPermissionsWithoutPermission() throws Exception {
        String token = jwtService.generateAccessToken(
                999L,
                List.of("PLAYER"),
                List.of("account:read:self")
        );

        mockMvc.perform(get("/roles/{roleId}/permissions", captainRoleEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private RoleEntity createRole(String roleName) {
        RoleEntity role = new RoleEntity();
        role.setName(roleName);
        role.setPermissions(new ArrayList<>());
        return roleRepository.save(role);
    }

    private AccountEntity createPersistedAccount(String email,
                                                 String rawPassword,
                                                 AccountStatus status,
                                                 RoleEntity... roleEntities) {
        AccountBuilder builder = validAccountBuilder(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(status);

        for (RoleEntity roleEntity : roleEntities) {
            Role roleModel = roleMapper.toModel(roleEntity);
            builder.addRole(roleModel);
        }

        Account account = builder.build();
        return accountRepository.save(accountMapper.toEntity(account));
    }
}