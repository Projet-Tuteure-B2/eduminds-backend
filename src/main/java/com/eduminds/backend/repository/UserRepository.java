package com.eduminds.backend.repository;

import com.eduminds.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Utile pour la validation post-diagnostic :
    // userRepository.findByEmail(email).map(User::getDiagnosticPassed)
}
