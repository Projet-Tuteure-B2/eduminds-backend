package com.eduminds.backend.repository;
import com.eduminds.backend.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByChapitreId(Long chapitreId);
    long countByChapitreId(Long chapitreId);
}
