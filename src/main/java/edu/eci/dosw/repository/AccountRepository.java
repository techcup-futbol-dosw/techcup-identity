package edu.eci.dosw.repository;

import edu.eci.dosw.entity.AccountEntity;
import edu.eci.dosw.model.AccountStatus;
import edu.eci.dosw.model.IdentificationType;
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

    @Query(
            value = """
                SELECT DISTINCT a
                FROM AccountEntity a
                LEFT JOIN a.roles r
                WHERE (:queryPattern = '' OR
                       LOWER(a.name) LIKE :queryPattern OR
                       LOWER(a.lastName) LIKE :queryPattern OR
                       LOWER(a.email) LIKE :queryPattern OR
                       LOWER(a.identification) LIKE :queryPattern)
                  AND (:role = '' OR LOWER(r.name) = :role)
                  AND (:status IS NULL OR a.status = :status)
                """,
            countQuery = """
                SELECT COUNT(DISTINCT a)
                FROM AccountEntity a
                LEFT JOIN a.roles r
                WHERE (:queryPattern = '' OR
                       LOWER(a.name) LIKE :queryPattern OR
                       LOWER(a.lastName) LIKE :queryPattern OR
                       LOWER(a.email) LIKE :queryPattern OR
                       LOWER(a.identification) LIKE :queryPattern)
                  AND (:role = '' OR LOWER(r.name) = :role)
                  AND (:status IS NULL OR a.status = :status)
                """
    )
    Page<AccountEntity> searchForAdmin(@Param("queryPattern") String queryPattern,
                                       @Param("role") String role,
                                       @Param("status") AccountStatus status,
                                       Pageable pageable);

    boolean existsByIdentificationTypeAndIdentificationIgnoreCase(
            IdentificationType identificationType,
            String identification
    );
}
