package we.rap.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import we.rap.backend.models.Auth;

public interface AuthRepository extends JpaRepository<Auth,UUID>{

    Optional<Auth> findByTokenHash(String tokenHash);

    List<Auth> findAllByUserId(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Auth a SET a.revoked = true
            WHERE a.revoked = false            
          """)
    int revokeAllExceptRole();
}
