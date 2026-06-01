package com.eduminds.backend.repository;
import com.eduminds.backend.entity.Chapitre;
import com.eduminds.backend.entity.StatutTraitement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ChapitreRepository extends JpaRepository<Chapitre, Long> {
    List<Chapitre> findByMatiereId(Long matiereId);
    List<Chapitre> findByMatiereIdAndStatut(Long matiereId, StatutTraitement statut);
}
