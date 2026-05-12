package edu.eci.dosw.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.dto.*;
import edu.eci.dosw.entity.*;
import edu.eci.dosw.mapper.*;
import edu.eci.dosw.model.*;
import edu.eci.dosw.repository.*;
import jakarta.transaction.Transactional;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NewIntegrationTest {

    private static final String ADMIN_ROLE = "ADMIN";

    private static final String ACCOUNT_READ_ANY =
            "account:read:any";

    private static final String GET_BY_EMAIL_PATH =
            "/accounts/email/{email}";

    private static final String SEARCH_ACCOUNTS_PATH =
            "/accounts";

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

    private RoleEntity adminRoleEntity;

    @BeforeEach
    void setUp() {
        adminRoleEntity = roleRepository
        .findByNameIgnoreCase(ADMIN_ROLE)
        .orElseGet(() ->
                roleRepository.save(
                        createRoleEntity(ADMIN_ROLE)
                )
        );
    }

    @Test
    @DisplayName("Should return account by email successfully")
    void shouldReturnAccountByEmailSuccessfully() throws Exception {

        String email = "account@mail.com";

        createPersistedAccount(
                email,
                adminRoleEntity
        );

        performGetByEmail(email)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value(email));
    }

    @Test
    @DisplayName("Should return 404 when account does not exist")
    void shouldReturnNotFoundWhenAccountDoesNotExist()
            throws Exception {

        performGetByEmail("missing@mail.com")
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should search accounts successfully")
    void shouldSearchAccountsSuccessfully() throws Exception {

        createPersistedAccount(
                "juan@mail.com",
                adminRoleEntity
        );

        performSearchAccounts()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    private AccountEntity createPersistedAccount(
            String email,
            RoleEntity roleEntity
    ) {

        Role roleModel =
                roleMapper.toModel(roleEntity);

        Account account = new Account(
                LocalDate.now(),
                LocalDateTime.now(),
                email,
                Gender.MALE,
                null,
                "123456",
                IdentificationType.CC,
                LocalDateTime.now(),
                "Perez",
                "Juan",
                passwordEncoder.encode("Password123*"),
                "Systems",
                Relation.ESTUDIANTE,
                List.of(roleModel),
                8,
                AccountStatus.ACTIVE,
                LocalDateTime.now()
        );

        return accountRepository.save(
                accountMapper.toEntity(account)
        );
    }

    private RoleEntity createRoleEntity(String roleName) {

        RoleEntity roleEntity =
                new RoleEntity();

        roleEntity.setName(roleName);
        roleEntity.setPermissions(new ArrayList<>());

        return roleEntity;
    }

    private ResultActions performGetByEmail(String email)
            throws Exception {

        return mockMvc.perform(
                get(GET_BY_EMAIL_PATH, email)
                        .with(
                                user("admin")
                                        .authorities(
                                                () -> ACCOUNT_READ_ANY
                                        )
                        )
                        .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private ResultActions performSearchAccounts()
            throws Exception {

        return mockMvc.perform(
                get(SEARCH_ACCOUNTS_PATH)
                        .param("query", "juan")
                        .param("page", "0")
                        .param("size", "10")
                        .with(
                                user("admin")
                                        .authorities(
                                                () -> ACCOUNT_READ_ANY
                                        )
                        )
                        .contentType(MediaType.APPLICATION_JSON)
        );
    }
}