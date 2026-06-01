package com.eduminds.backend.repository;
import com.eduminds.backend.entity.ProgressionCarte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface ProgressionCarteRepository extends JpaRepository<ProgressionCarte, Long> {
    Optional<ProgressionCarte> findByFlashcardIdAndUserId(Long flashcardId, Long userId);
    @Query("SELECT p FROM ProgressionCarte p WHERE p.user.id = :userId AND p.prochaineRevision <= :today")
    List<ProgressionCarte> findCartesAReviser(@Param("userId") Long userId, @Param("today") LocalDate today);
    long countByUserIdAndBoite(Long userId, Integer boite);
}
