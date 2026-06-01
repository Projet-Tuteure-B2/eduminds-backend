package com.eduminds.backend.repository;
import com.eduminds.backend.entity.EvaluationPostRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface EvaluationPostRevisionRepository extends JpaRepository<EvaluationPostRevision, Long> {
    Optional<EvaluationPostRevision> findBySessionRevisionId(Long sessionId);
    List<EvaluationPostRevision> findByUserIdOrderByDateDebutDesc(Long userId);
}
