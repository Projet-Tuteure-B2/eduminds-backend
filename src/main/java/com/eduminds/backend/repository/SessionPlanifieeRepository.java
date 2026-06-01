package com.eduminds.backend.repository;
import com.eduminds.backend.entity.SessionPlanifiee;
import com.eduminds.backend.entity.StatutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
@Repository
public interface SessionPlanifieeRepository extends JpaRepository<SessionPlanifiee, Long> {
    List<SessionPlanifiee> findByPlanningIdAndDatePrevueBetweenOrderByDatePrevueAsc(Long planningId, LocalDate debut, LocalDate fin);
    List<SessionPlanifiee> findByPlanningIdAndStatutOrderByDatePrevueAsc(Long planningId, StatutSession statut);
}
