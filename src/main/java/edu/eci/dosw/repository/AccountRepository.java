package edu.eci.dosw.repository;

import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.model.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByEmail(String email);

    @Query("""
    SELECT DISTINCT a
    FROM AccountEntity a
    LEFT JOIN a.roles r
    WHERE (:query IS NULL OR
           LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
           LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR
           LOWER(a.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
           LOWER(a.identification) LIKE LOWER(CONCAT('%', :query, '%')))
      AND (:role IS NULL OR LOWER(r.name) = LOWER(:role))
      AND (:status IS NULL OR a.status = :status)
""")
    Page<AccountEntity> searchForAdmin(@Param("query") String query,
                                       @Param("role") String role,
                                       @Param("status") AccountStatus status,
                                       Pageable pageable);
}
