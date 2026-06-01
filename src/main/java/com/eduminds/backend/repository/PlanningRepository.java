package com.eduminds.backend.repository;
import com.eduminds.backend.entity.Planning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface PlanningRepository extends JpaRepository<Planning, Long> {
    Optional<Planning> findByUserId(Long userId);
}
