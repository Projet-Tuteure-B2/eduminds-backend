package com.eduminds.backend.repository;
import com.eduminds.backend.entity.JourneeRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface JourneeRevisionRepository extends JpaRepository<JourneeRevision, Long> {
    Optional<JourneeRevision> findByUserIdAndDate(Long userId, LocalDate date);
    List<JourneeRevision> findByUserIdAndDateBetweenOrderByDate(Long userId, LocalDate debut, LocalDate fin);
}
