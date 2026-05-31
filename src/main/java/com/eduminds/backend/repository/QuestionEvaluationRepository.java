package com.eduminds.backend.repository;
import com.eduminds.backend.entity.QuestionEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface QuestionEvaluationRepository extends JpaRepository<QuestionEvaluation, Long> {
    List<QuestionEvaluation> findByEvaluationIdOrderByNumeroOrdre(Long evaluationId);
}
