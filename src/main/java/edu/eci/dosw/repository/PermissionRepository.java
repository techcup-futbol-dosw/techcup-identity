package edu.eci.dosw.repository;

import edu.eci.dosw.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    Optional<PermissionEntity> findByNameIgnoreCase(String permissionName);
}
