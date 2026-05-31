package com.eduminds.backend.repository;
import com.eduminds.backend.entity.ParticipationChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface ParticipationChallengeRepository extends JpaRepository<ParticipationChallenge, Long> {
    Optional<ParticipationChallenge> findBySalleIdAndUserId(Long salleId, Long userId);
    List<ParticipationChallenge> findBySalleIdOrderByScoreDesc(Long salleId);
}
