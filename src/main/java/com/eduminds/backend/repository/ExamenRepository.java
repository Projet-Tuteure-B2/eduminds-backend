package com.eduminds.backend.repository;
import com.eduminds.backend.entity.Examen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {
    List<Examen> findByUserIdOrderByDateExamenAsc(Long userId);
    List<Examen> findByUserIdAndDateExamenAfterOrderByDateExamenAsc(Long userId, LocalDate today);
}
