package edu.eci.dosw.unitary.config;

import edu.eci.dosw.config.RoleDataInitializer;
import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.entity.RoleEntity;
import edu.eci.dosw.repository.PermissionRepository;
import edu.eci.dosw.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleDataInitializerTest {

    private static final String PLAYER = "PLAYER";
    private static final String CAPTAIN = "CAPTAIN";
    private static final String ORGANIZER = "ORGANIZER";
    private static final String REFEREE = "REFEREE";
    private static final String ADMIN = "ADMIN";

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    private RoleDataInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new RoleDataInitializer();
    }

    @Test
    void roleInitializer_ShouldCreateAllRoles_WhenRolesDoNotExist() throws Exception {
        List<PermissionEntity> allPermissions = allPermissionEntities();

        when(permissionRepository.findAll()).thenReturn(allPermissions);
        mockRoleLookupAsMissing();

        CommandLineRunner runner = initializer.roleInitializer(roleRepository, permissionRepository);
        runner.run();

        ArgumentCaptor<RoleEntity> captor = ArgumentCaptor.forClass(RoleEntity.class);

        verify(permissionRepository).findAll();
        verify(roleRepository, times(5)).save(captor.capture());

        List<RoleEntity> savedRoles = captor.getAllValues();

        assertRolePermissionsSize(savedRoles, PLAYER, getPermissionSet("PLAYER_PERMISSIONS").size());
        assertRolePermissionsSize(savedRoles, CAPTAIN, expectedCaptainPermissions().size());
        assertRolePermissionsSize(savedRoles, ORGANIZER, getPermissionSet("ORGANIZER_PERMISSIONS").size());
        assertRolePermissionsSize(savedRoles, REFEREE, getPermissionSet("REFEREE_PERMISSIONS").size());
        assertRolePermissionsSize(savedRoles, ADMIN, allPermissions.size());
    }

    @Test
    void roleInitializer_ShouldUpdateExistingRoles_WhenRolesAlreadyExist() throws Exception {
        List<PermissionEntity> allPermissions = allPermissionEntities();

        RoleEntity playerRole = role(PLAYER);
        RoleEntity captainRole = role(CAPTAIN);
        RoleEntity organizerRole = role(ORGANIZER);
        RoleEntity refereeRole = role(REFEREE);
        RoleEntity adminRole = role(ADMIN);

        when(permissionRepository.findAll()).thenReturn(allPermissions);

        when(roleRepository.findByNameIgnoreCase(PLAYER)).thenReturn(Optional.of(playerRole));
        when(roleRepository.findByNameIgnoreCase(CAPTAIN)).thenReturn(Optional.of(captainRole));
        when(roleRepository.findByNameIgnoreCase(ORGANIZER)).thenReturn(Optional.of(organizerRole));
        when(roleRepository.findByNameIgnoreCase(REFEREE)).thenReturn(Optional.of(refereeRole));
        when(roleRepository.findByNameIgnoreCase(ADMIN)).thenReturn(Optional.of(adminRole));

        CommandLineRunner runner = initializer.roleInitializer(roleRepository, permissionRepository);
        runner.run();

        verify(roleRepository, times(5)).save(any(RoleEntity.class));

        assertEquals(getPermissionSet("PLAYER_PERMISSIONS").size(), playerRole.getPermissions().size());
        assertEquals(expectedCaptainPermissions().size(), captainRole.getPermissions().size());
        assertEquals(getPermissionSet("ORGANIZER_PERMISSIONS").size(), organizerRole.getPermissions().size());
        assertEquals(getPermissionSet("REFEREE_PERMISSIONS").size(), refereeRole.getPermissions().size());
        assertEquals(allPermissions.size(), adminRole.getPermissions().size());
    }

    @Test
    void roleInitializer_ShouldAssignOnlyExistingPermissions_WhenSomePermissionsAreMissing() throws Exception {
        Set<String> onlyPlayerPermissions = getPermissionSet("PLAYER_PERMISSIONS");

        List<PermissionEntity> availablePermissions = onlyPlayerPermissions.stream()
                .map(this::permission)
                .toList();

        when(permissionRepository.findAll()).thenReturn(availablePermissions);
        mockRoleLookupAsMissing();

        CommandLineRunner runner = initializer.roleInitializer(roleRepository, permissionRepository);
        runner.run();

        ArgumentCaptor<RoleEntity> captor = ArgumentCaptor.forClass(RoleEntity.class);
        verify(roleRepository, times(5)).save(captor.capture());

        List<RoleEntity> savedRoles = captor.getAllValues();

        RoleEntity playerRole = findRole(savedRoles, PLAYER);
        RoleEntity captainRole = findRole(savedRoles, CAPTAIN);
        RoleEntity organizerRole = findRole(savedRoles, ORGANIZER);
        RoleEntity refereeRole = findRole(savedRoles, REFEREE);
        RoleEntity adminRole = findRole(savedRoles, ADMIN);

        assertEquals(onlyPlayerPermissions.size(), playerRole.getPermissions().size());
        assertEquals(onlyPlayerPermissions.size(), captainRole.getPermissions().size());

        assertEquals(
                intersectionSize(onlyPlayerPermissions, getPermissionSet("ORGANIZER_PERMISSIONS")),
                organizerRole.getPermissions().size()
        );

        assertEquals(
                intersectionSize(onlyPlayerPermissions, getPermissionSet("REFEREE_PERMISSIONS")),
                refereeRole.getPermissions().size()
        );

        assertEquals(availablePermissions.size(), adminRole.getPermissions().size());
    }

    @Test
    void roleInitializer_ShouldHandleMixedCasePermissionNames_WhenBuildingPermissionMap() throws Exception {
        List<PermissionEntity> permissions = List.of(
                permission("ACCOUNT:READ:SELF"),
                permission("Team:Create:Own"),
                permission("Referee-Match:Read:Assigned")
        );

        when(permissionRepository.findAll()).thenReturn(permissions);
        mockRoleLookupAsMissing();

        CommandLineRunner runner = initializer.roleInitializer(roleRepository, permissionRepository);
        runner.run();

        ArgumentCaptor<RoleEntity> captor = ArgumentCaptor.forClass(RoleEntity.class);
        verify(roleRepository, times(5)).save(captor.capture());

        List<RoleEntity> savedRoles = captor.getAllValues();

        RoleEntity playerRole = findRole(savedRoles, PLAYER);
        RoleEntity captainRole = findRole(savedRoles, CAPTAIN);
        RoleEntity refereeRole = findRole(savedRoles, REFEREE);
        RoleEntity adminRole = findRole(savedRoles, ADMIN);

        assertTrue(containsPermission(playerRole, "ACCOUNT:READ:SELF"));
        assertTrue(containsPermission(captainRole, "Team:Create:Own"));
        assertTrue(containsPermission(refereeRole, "Referee-Match:Read:Assigned"));
        assertEquals(permissions.size(), adminRole.getPermissions().size());
    }

    private void mockRoleLookupAsMissing() {
        when(roleRepository.findByNameIgnoreCase(PLAYER)).thenReturn(Optional.empty());
        when(roleRepository.findByNameIgnoreCase(CAPTAIN)).thenReturn(Optional.empty());
        when(roleRepository.findByNameIgnoreCase(ORGANIZER)).thenReturn(Optional.empty());
        when(roleRepository.findByNameIgnoreCase(REFEREE)).thenReturn(Optional.empty());
        when(roleRepository.findByNameIgnoreCase(ADMIN)).thenReturn(Optional.empty());
    }

    private RoleEntity role(String name) {
        RoleEntity role = new RoleEntity();
        role.setName(name);
        role.setPermissions(new ArrayList<>());
        return role;
    }

    private PermissionEntity permission(String name) {
        PermissionEntity permission = new PermissionEntity();
        permission.setName(name);
        return permission;
    }

    private List<PermissionEntity> allPermissionEntities() throws Exception {
        return allConfiguredPermissions().stream()
                .map(this::permission)
                .toList();
    }

    private void assertRolePermissionsSize(List<RoleEntity> savedRoles,
                                           String roleName,
                                           int expectedSize) {
        RoleEntity role = findRole(savedRoles, roleName);
        assertEquals(expectedSize, role.getPermissions().size());
    }

    private RoleEntity findRole(List<RoleEntity> roles, String roleName) {
        return roles.stream()
                .filter(role -> roleName.equals(role.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Role not found in saved roles: " + roleName));
    }

    private boolean containsPermission(RoleEntity role, String permissionName) {
        return role.getPermissions().stream()
                .map(PermissionEntity::getName)
                .anyMatch(permissionName::equals);
    }

    @SuppressWarnings("unchecked")
    private Set<String> getPermissionSet(String fieldName) throws Exception {
        Field field = RoleDataInitializer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Set<String>) field.get(null);
    }

    private Set<String> expectedCaptainPermissions() throws Exception {
        Set<String> merged = new LinkedHashSet<>(getPermissionSet("PLAYER_PERMISSIONS"));
        merged.addAll(getPermissionSet("CAPTAIN_EXTRA_PERMISSIONS"));
        return merged;
    }

    private Set<String> allConfiguredPermissions() throws Exception {
        Set<String> all = new LinkedHashSet<>();
        all.addAll(getPermissionSet("PLAYER_PERMISSIONS"));
        all.addAll(getPermissionSet("CAPTAIN_EXTRA_PERMISSIONS"));
        all.addAll(getPermissionSet("ORGANIZER_PERMISSIONS"));
        all.addAll(getPermissionSet("REFEREE_PERMISSIONS"));
        return all;
    }
    private int intersectionSize(Set<String> available, Set<String> expectedForRole) {
        Set<String> copy = new LinkedHashSet<>(available);
        copy.retainAll(expectedForRole);
        return copy.size();
    }
}