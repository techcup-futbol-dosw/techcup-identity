package edu.eci.dosw.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.dto.AssignRoleRequest;
import edu.eci.dosw.dto.RemoveRoleRequest;
import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.Account;
import edu.eci.dosw.model.AccountBuilder;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.model.Role;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

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

    private static final String PLAYER = "PLAYER";
    private static final String CAPTAIN = "CAPTAIN";
    private static final String ADMIN = "ADMIN";
    private static final String UNKNOWN_ROLE = "UNKNOWN_ROLE";

    private static final String DEFAULT_PASSWORD = "Password123*";

    private static final String ROLE_ASSIGN_ANY = "role:assign:any";
    private static final String ROLE_REMOVE_ANY = "role:remove:any";
    private static final String ROLE_READ_ANY = "role:read:any";
    private static final String PERMISSION_READ_ANY = "permission:read:any";
    private static final String ACCOUNT_READ_SELF = "account:read:self";

    private static final String TEAM_CREATE_OWN = "team:create:own";

    private static final Long ADMIN_ACCOUNT_ID = 999L;
    private static final Long MISSING_ACCOUNT_ID = 99999L;

    private static final String ASSIGN_PATH = "/roles/assign";
    private static final String REMOVE_PATH = "/roles/remove";
    private static final String ACCOUNT_ROLES_PATH = "/roles/account/{accountId}";
    private static final String ROLE_PERMISSIONS_PATH = "/roles/{roleId}/permissions";

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

        playerRoleEntity = createRole(PLAYER);
        captainRoleEntity = createRole(CAPTAIN);
        adminRoleEntity = createRole(ADMIN);
    }
    // =========================================================
    // ASSIGN ROLE
    // =========================================================

    @Test
    @DisplayName("Should assign role successfully when caller has permission")
    void shouldAssignRoleSuccessfully() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "assign@mail.escuelaing.edu.co",
                playerRoleEntity
        );

        performAssignRole(
                assignRoleRequest(accountEntity.getId(), CAPTAIN),
                adminToken(ROLE_ASSIGN_ANY)
        ).andExpect(status().isNoContent());

        AccountEntity updatedAccount = accountRepository
                .findById(accountEntity.getId())
                .orElseThrow();

        assertThat(updatedAccount.getRoles())
                .extracting(RoleEntity::getName)
                .contains(PLAYER, CAPTAIN);
    }

    @Test
    @DisplayName("Should return 403 when caller tries to assign role without permission")
    void shouldReturnForbiddenWhenAssigningWithoutPermission() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "forbidden-assign@mail.escuelaing.edu.co",
                playerRoleEntity
        );

        performAssignRole(
                assignRoleRequest(accountEntity.getId(), CAPTAIN),
                playerToken(ACCOUNT_READ_SELF)
        ).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when assigning role to non-existing account")
    void shouldReturnNotFoundWhenAssigningRoleToMissingAccount() throws Exception {
        performAssignRole(
                assignRoleRequest(MISSING_ACCOUNT_ID, CAPTAIN),
                adminToken(ROLE_ASSIGN_ANY)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Should return 404 when assigning non-existing role")
    void shouldReturnNotFoundWhenAssigningMissingRole() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "missingrole@mail.escuelaing.edu.co",
                playerRoleEntity
        );

        performAssignRole(
                assignRoleRequest(accountEntity.getId(), UNKNOWN_ROLE),
                adminToken(ROLE_ASSIGN_ANY)
        )
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
                playerRoleEntity,
                captainRoleEntity
        );

        performRemoveRole(
                removeRoleRequest(accountEntity.getId(), CAPTAIN),
                adminToken(ROLE_REMOVE_ANY)
        ).andExpect(status().isNoContent());

        AccountEntity updatedAccount = accountRepository
                .findById(accountEntity.getId())
                .orElseThrow();

        assertThat(updatedAccount.getRoles())
                .extracting(RoleEntity::getName)
                .contains(PLAYER)
                .doesNotContain(CAPTAIN);
    }

    // =========================================================
    // GET ROLES BY ACCOUNT
    // =========================================================

    @Test
    @DisplayName("Should return roles by account when caller has read permission")
    void shouldReturnRolesByAccountSuccessfully() throws Exception {
        AccountEntity accountEntity = createPersistedAccount(
                "roles@mail.escuelaing.edu.co",
                playerRoleEntity,
                captainRoleEntity
        );

        mockMvc.perform(get(ACCOUNT_ROLES_PATH, accountEntity.getId())
                        .header("Authorization", bearer(adminToken(ROLE_READ_ANY))))
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
        PermissionEntity permission = createPermission(TEAM_CREATE_OWN);

        captainRoleEntity.setPermissions(new ArrayList<>(List.of(permission)));
        captainRoleEntity = roleRepository.save(captainRoleEntity);

        mockMvc.perform(get(ROLE_PERMISSIONS_PATH, captainRoleEntity.getId())
                        .header("Authorization", bearer(adminToken(PERMISSION_READ_ANY))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(TEAM_CREATE_OWN));
    }
    @Test
    @DisplayName("Should return 403 when caller tries to read permissions without permission")
    void shouldReturnForbiddenWhenReadingPermissionsWithoutPermission() throws Exception {
        mockMvc.perform(get(ROLE_PERMISSIONS_PATH, captainRoleEntity.getId())
                        .header("Authorization", bearer(playerToken(ACCOUNT_READ_SELF))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return all roles when caller has read permission")
    void shouldReturnAllRolesSuccessfully() throws Exception {
        String token = jwtService.generateAccessToken(
                999L,
                List.of("ADMIN"),
                List.of("role:read:any")
        );

        mockMvc.perform(get("/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[*].name").isArray());
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private ResultActions performAssignRole(AssignRoleRequest request, String token) throws Exception {
        return performPost(ASSIGN_PATH, request, token);
    }

    private ResultActions performRemoveRole(RemoveRoleRequest request, String token) throws Exception {
        return performPost(REMOVE_PATH, request, token);
    }

    private ResultActions performPost(String path, Object request, String token) throws Exception {
        return mockMvc.perform(post(path)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private AssignRoleRequest assignRoleRequest(Long accountId, String roleName) {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setAccountId(accountId);
        request.setRoleName(roleName);
        return request;
    }

    private RemoveRoleRequest removeRoleRequest(Long accountId, String roleName) {
        RemoveRoleRequest request = new RemoveRoleRequest();
        request.setAccountId(accountId);
        request.setRoleName(roleName);
        return request;
    }

    private String adminToken(String permission) {
        return accessToken(
                ADMIN_ACCOUNT_ID,
                List.of(ADMIN),
                List.of(permission)
        );
    }

    private String playerToken(String permission) {
        return accessToken(
                ADMIN_ACCOUNT_ID,
                List.of(PLAYER),
                List.of(permission)
        );
    }

    private String accessToken(Long accountId, List<String> roles, List<String> permissions) {
        return jwtService.generateAccessToken(accountId, roles, permissions);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private RoleEntity createRole(String roleName) {
        RoleEntity role = new RoleEntity();
        role.setName(roleName);
        role.setPermissions(new ArrayList<>());
        return roleRepository.save(role);
    }

    private PermissionEntity createPermission(String permissionName) {
        PermissionEntity permission = new PermissionEntity();
        permission.setName(permissionName);
        return permissionRepository.save(permission);
    }

    private AccountEntity createPersistedAccount(String email, RoleEntity... roleEntities) {
        AccountBuilder builder = validAccountBuilder(email)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .status(AccountStatus.ACTIVE);

        for (RoleEntity roleEntity : roleEntities) {
            Role roleModel = roleMapper.toModel(roleEntity);
            builder.addRole(roleModel);
        }

        Account account = builder.build();
        return accountRepository.save(accountMapper.toEntity(account));
    }
}