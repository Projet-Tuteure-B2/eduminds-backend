package com.eduminds.backend.repository;
import com.eduminds.backend.entity.FichierSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface FichierSourceRepository extends JpaRepository<FichierSource, Long> {
    List<FichierSource> findByChapitreId(Long chapitreId);
}
