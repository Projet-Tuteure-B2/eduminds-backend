package com.eduminds.backend.repository;
import com.eduminds.backend.entity.ReponseChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface ReponseChallengeRepository extends JpaRepository<ReponseChallenge, Long> {
    Optional<ReponseChallenge> findByQuestionIdAndParticipantId(Long questionId, Long participantId);
    List<ReponseChallenge> findByParticipantId(Long participantId);
}
