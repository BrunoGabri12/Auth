package we.rap.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import we.rap.backend.models.User;


public interface UserRepository extends JpaRepository<User, UUID>{

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);


    Optional<User> findById(UUID userId);
}
