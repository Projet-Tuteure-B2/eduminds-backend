package com.eduminds.backend.repository;
import com.eduminds.backend.entity.Preferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface PreferencesRepository extends JpaRepository<Preferences, Long> {
    Optional<Preferences> findByUserId(Long userId);
}
