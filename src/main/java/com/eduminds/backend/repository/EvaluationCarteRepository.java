package com.eduminds.backend.repository;
import com.eduminds.backend.entity.EvaluationCarte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface EvaluationCarteRepository extends JpaRepository<EvaluationCarte, Long> {
    List<EvaluationCarte> findBySessionId(Long sessionId);
}
