package edu.eci.dosw.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.model.Program;
import edu.eci.dosw.dto.RegisterAccountRequest;
import edu.eci.dosw.model.Relation;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.mapper.AccountMapper;
import edu.eci.dosw.mapper.RoleMapper;
import edu.eci.dosw.model.*;
import edu.eci.dosw.repository.AccountRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private RoleEntity adminRoleEntity;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        playerRoleEntity = new RoleEntity();
        playerRoleEntity.setName("PLAYER");
        playerRoleEntity.setPermissions(new ArrayList<>());
        playerRoleEntity = roleRepository.save(playerRoleEntity);

        adminRoleEntity = new RoleEntity();
        adminRoleEntity.setName("ADMIN");
        adminRoleEntity.setPermissions(new ArrayList<>());
        adminRoleEntity = roleRepository.save(adminRoleEntity);
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @Test
    @DisplayName("Should register account successfully")
    void shouldRegisterAccountSuccessfully() throws Exception {
        RegisterAccountRequest request = createValidRegisterRequest("juan@mail.escuelaing.edu.co");

        mockMvc.perform(post("/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("juan@mail.escuelaing.edu.co"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Roa"))
                .andExpect(jsonPath("$.relation").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.semester").value(7))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.identification").value("123456789"));

        assertThat(accountRepository.findByEmail("juan@mail.escuelaing.edu.co")).isPresent();
    }

    @Test
    @DisplayName("Should return 409 when email is already registered")
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        RegisterAccountRequest request = createValidRegisterRequest("juan@mail.escuelaing.edu.co");

        mockMvc.perform(post("/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 when student semester is missing")
    void shouldReturnBadRequestWhenStudentSemesterIsMissing() throws Exception {
        RegisterAccountRequest request = createValidRegisterRequest("maria@mail.escuelaing.edu.co");
        request.setSemester(null);

        mockMvc.perform(post("/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 when family email is not Gmail")
    void shouldReturnBadRequestWhenFamilyEmailIsNotGmail() throws Exception {
        RegisterAccountRequest request = createValidRegisterRequest("family@mail.escuelaing.edu.co");
        request.setRelation(Relation.FAMILIAR);

        mockMvc.perform(post("/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 when institutional email is invalid")
    void shouldReturnBadRequestWhenInstitutionalEmailIsInvalid() throws Exception {
        RegisterAccountRequest request = createValidRegisterRequest("juan@gmail.com");
        request.setRelation(Relation.ESTUDIANTE);

        mockMvc.perform(post("/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

        // =========================================================
        // EXISTS BY EMAIL
        // =========================================================

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists () throws Exception {
            createPersistedAccount("exists@mail.escuelaing.edu.co", playerRoleEntity);

            mockMvc.perform(get("/accounts/exists")
                            .param("email", "exists@mail.escuelaing.edu.co"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist () throws Exception {
            mockMvc.perform(get("/accounts/exists")
                            .param("email", "missing@mail.escuelaing.edu.co"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @Test
        @DisplayName("Should allow self account read")
        void shouldAllowSelfAccountRead () throws Exception {
            var accountEntity = createPersistedAccount("self@mail.escuelaing.edu.co", playerRoleEntity);

            String token = jwtService.generateAccessToken(
                    accountEntity.getId(),
                    List.of("PLAYER"),
                    List.of("account:read:self")
            );

            mockMvc.perform(get("/accounts/{id}", accountEntity.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("self@mail.escuelaing.edu.co"));
        }

        @Test
        @DisplayName("Should allow admin to read any account")
        void shouldAllowAdminToReadAnyAccount () throws Exception {
            var accountEntity = createPersistedAccount("user@mail.escuelaing.edu.co", playerRoleEntity);

            String token = jwtService.generateAccessToken(
                    999L,
                    List.of("ADMIN"),
                    List.of("account:read:any")
            );

            mockMvc.perform(get("/accounts/{id}", accountEntity.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("user@mail.escuelaing.edu.co"));
        }

        @Test
        @DisplayName("Should return 403 when user tries to read another account without read:any")
        void shouldReturnForbiddenWhenReadingAnotherAccountWithoutPermission () throws Exception {
            var ownerAccount = createPersistedAccount("owner@mail.escuelaing.edu.co", playerRoleEntity);
            var otherAccount = createPersistedAccount("other@mail.escuelaing.edu.co", playerRoleEntity);

            String token = jwtService.generateAccessToken(
                    ownerAccount.getId(),
                    List.of("PLAYER"),
                    List.of("account:read:self")
            );

            mockMvc.perform(get("/accounts/{id}", otherAccount.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when account does not exist")
        void shouldReturnNotFoundWhenAccountDoesNotExist () throws Exception {
            String token = jwtService.generateAccessToken(
                    999L,
                    List.of("ADMIN"),
                    List.of("account:read:any")
            );

            mockMvc.perform(get("/accounts/{id}", 99999L)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        // =========================================================
        // DEACTIVATE
        // =========================================================

        @Test
        @DisplayName("Should deactivate account when caller has permission")
        void shouldDeactivateAccountWhenCallerHasPermission () throws Exception {
            var accountEntity = createPersistedAccount("deactivate@mail.escuelaing.edu.co", playerRoleEntity);

            String token = jwtService.generateAccessToken(
                    999L,
                    List.of("ADMIN"),
                    List.of("account:deactivate:any")
            );

            mockMvc.perform(patch("/accounts/{id}/deactivate", accountEntity.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());

            var updated = accountRepository.findById(accountEntity.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        }

        @Test
        @DisplayName("Should return 403 when caller tries to deactivate without permission")
        void shouldReturnForbiddenWhenDeactivatingWithoutPermission () throws Exception {
            var accountEntity = createPersistedAccount("nodeactivate@mail.escuelaing.edu.co", playerRoleEntity);

            String token = jwtService.generateAccessToken(
                    999L,
                    List.of("PLAYER"),
                    List.of("account:read:self")
            );

            mockMvc.perform(patch("/accounts/{id}/deactivate", accountEntity.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }

        // =========================================================
        // HELPERS
        // =========================================================

    private edu.eci.dosw.entity.AccountEntity createPersistedAccount(String email, RoleEntity roleEntity) {
        Role roleModel = roleMapper.toModel(roleEntity);

        Account account = new AccountBuilder()
                .name("Juan")
                .lastName("Roa")
                .birthDate(LocalDate.of(2000, 5, 15))
                .relation(Relation.ESTUDIANTE)
                .semester(7)
                .program("SISTEMAS")
                .email(email)
                .passwordHash(passwordEncoder.encode("Password123*"))
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .gender(Gender.MALE)
                .identificationType(IdentificationType.CC)
                .identification("ID-" + Math.abs(email.hashCode()))
                .addRole(roleModel)
                .build();

        return accountRepository.save(accountMapper.toEntity(account));
    }
    private RegisterAccountRequest createValidRegisterRequest (String email){
            RegisterAccountRequest request = new RegisterAccountRequest();

            request.setEmail(email);
            request.setPassword("Password123*");
            request.setRelation(Relation.ESTUDIANTE);
            request.setProgram(Program.SISTEMAS);
            request.setSemester(7);

            request.setName("Juan");
            request.setLastName("Roa");
            request.setBirthDate(LocalDate.of(2000, 5, 15));
            request.setGender(Gender.MALE);
            request.setIdentificationType(IdentificationType.CC);
            request.setIdentification("123456789");

            return request;
    }
}