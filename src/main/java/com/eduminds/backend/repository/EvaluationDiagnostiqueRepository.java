package com.eduminds.backend.repository;
import com.eduminds.backend.entity.EvaluationDiagnostique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface EvaluationDiagnostiqueRepository extends JpaRepository<EvaluationDiagnostique, Long> {
    Optional<EvaluationDiagnostique> findFirstByUserIdAndTermineeOrderByDateDebutDesc(Long userId, Boolean terminee);
    List<EvaluationDiagnostique> findByUserIdOrderByDateDebutDesc(Long userId);
}
