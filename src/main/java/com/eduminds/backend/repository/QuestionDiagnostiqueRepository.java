package com.eduminds.backend.repository;
import com.eduminds.backend.entity.QuestionDiagnostique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface QuestionDiagnostiqueRepository extends JpaRepository<QuestionDiagnostique, Long> {
    List<QuestionDiagnostique> findByDiagnosticIdOrderByNumeroOrdre(Long diagnosticId);
}
