package com.eduminds.backend.repository;
import com.eduminds.backend.entity.QuestionChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface QuestionChallengeRepository extends JpaRepository<QuestionChallenge, Long> {
    List<QuestionChallenge> findBySalleIdOrderByNumeroOrdre(Long salleId);
}
