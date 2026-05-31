package com.eduminds.backend.repository;
import com.eduminds.backend.entity.ReponseEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ReponseEvaluationRepository extends JpaRepository<ReponseEvaluation, Long> {
}
