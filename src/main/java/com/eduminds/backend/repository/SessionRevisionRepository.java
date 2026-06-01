package com.eduminds.backend.repository;
import com.eduminds.backend.entity.SessionRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface SessionRevisionRepository extends JpaRepository<SessionRevision, Long> {
    List<SessionRevision> findByUserIdOrderByDateDebutDesc(Long userId);
    List<SessionRevision> findByUserIdAndTerminee(Long userId, Boolean terminee);
}
