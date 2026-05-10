package edu.eci.dosw.unitaria.config;

import edu.eci.dosw.config.PermissionDataInitializer;
import edu.eci.dosw.entity.PermissionEntity;
import edu.eci.dosw.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.CommandLineRunner;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionDataInitializerTest {

    @Mock
    private PermissionRepository permissionRepository;

    private PermissionDataInitializer initializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        initializer = new PermissionDataInitializer();
    }

    @Test
    void permissionInitializer_ShouldCreateAllMissingPermissions_WhenRepositoryIsEmpty() throws Exception {
        when(permissionRepository.findAll()).thenReturn(List.of());

        CommandLineRunner runner = initializer.permissionInitializer(permissionRepository);
        runner.run();

        ArgumentCaptor<List<PermissionEntity>> captor = ArgumentCaptor.forClass(List.class);

        verify(permissionRepository).findAll();
        verify(permissionRepository).saveAll(captor.capture());

        List<PermissionEntity> savedPermissions = captor.getValue();
        List<String> savedNames = extractNames(savedPermissions);
        Set<String> configuredPermissions = configuredPermissions();

        assertEquals(configuredPermissions.size(), savedNames.size());
        assertEquals(configuredPermissions.size(), new HashSet<>(savedNames).size());

        assertTrue(savedNames.contains("account:create:any"));
        assertTrue(savedNames.contains("role:assign:any"));
        assertTrue(savedNames.contains("team:create:own"));
        assertTrue(savedNames.contains("referee-match:read:assigned"));

        assertTrue(savedNames.stream().allMatch(name -> name.equals(name.toLowerCase(Locale.ROOT))));
    }

    @Test
    void permissionInitializer_ShouldCreateOnlyMissingPermissions_WhenSomeAlreadyExist() throws Exception {
        PermissionEntity existing1 = permission("ACCOUNT:CREATE:ANY");
        PermissionEntity existing2 = permission("role:assign:any");
        PermissionEntity ignoredBlank = permission("   ");
        PermissionEntity ignoredNull = permission(null);

        when(permissionRepository.findAll())
                .thenReturn(List.of(existing1, existing2, ignoredBlank, ignoredNull));

        CommandLineRunner runner = initializer.permissionInitializer(permissionRepository);
        runner.run();

        ArgumentCaptor<List<PermissionEntity>> captor = ArgumentCaptor.forClass(List.class);

        verify(permissionRepository).findAll();
        verify(permissionRepository).saveAll(captor.capture());

        List<PermissionEntity> savedPermissions = captor.getValue();
        List<String> savedNames = extractNames(savedPermissions);
        Set<String> configuredPermissions = configuredPermissions();

        assertFalse(savedNames.contains("account:create:any"));
        assertFalse(savedNames.contains("role:assign:any"));

        assertTrue(savedNames.contains("account:read:self"));
        assertTrue(savedNames.contains("permission:read:any"));

        assertEquals(configuredPermissions.size() - 2, savedNames.size());
    }

    @Test
    void permissionInitializer_ShouldNotSave_WhenAllPermissionsAlreadyExist() throws Exception {
        List<PermissionEntity> existingPermissions = configuredPermissions()
                .stream()
                .map(this::permission)
                .toList();

        when(permissionRepository.findAll()).thenReturn(existingPermissions);

        CommandLineRunner runner = initializer.permissionInitializer(permissionRepository);
        runner.run();

        verify(permissionRepository).findAll();
        verify(permissionRepository, never()).saveAll(any());
    }

    @Test
    void permissionInitializer_ShouldIgnoreNullAndBlankNames_WhenCollectingExistingPermissions() throws Exception {
        PermissionEntity blank = permission("   ");
        PermissionEntity nullName = permission(null);

        when(permissionRepository.findAll()).thenReturn(List.of(blank, nullName));

        CommandLineRunner runner = initializer.permissionInitializer(permissionRepository);
        runner.run();

        ArgumentCaptor<List<PermissionEntity>> captor = ArgumentCaptor.forClass(List.class);

        verify(permissionRepository).saveAll(captor.capture());

        List<PermissionEntity> savedPermissions = captor.getValue();
        Set<String> configuredPermissions = configuredPermissions();

        assertEquals(configuredPermissions.size(), savedPermissions.size());
    }

    private PermissionEntity permission(String name) {
        PermissionEntity permission = new PermissionEntity();
        permission.setName(name);
        return permission;
    }

    private List<String> extractNames(List<PermissionEntity> permissions) {
        return permissions.stream()
                .map(PermissionEntity::getName)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private Set<String> configuredPermissions() throws Exception {
        Field field = PermissionDataInitializer.class.getDeclaredField("PERMISSIONS");
        field.setAccessible(true);
        return (Set<String>) field.get(null);
    }
}
