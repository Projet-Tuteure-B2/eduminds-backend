package com.eduminds.backend.repository;
import com.eduminds.backend.entity.ReponseDiagnostique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ReponseDiagnostiqueRepository extends JpaRepository<ReponseDiagnostique, Long> {
}
