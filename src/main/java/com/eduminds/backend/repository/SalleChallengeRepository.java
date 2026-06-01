package com.eduminds.backend.repository;
import com.eduminds.backend.entity.SalleChallenge;
import com.eduminds.backend.entity.StatutChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface SalleChallengeRepository extends JpaRepository<SalleChallenge, Long> {
    Optional<SalleChallenge> findByCodeAcces(String codeAcces);
    List<SalleChallenge> findByHoteIdOrderByDateCreationDesc(Long hoteId);
    List<SalleChallenge> findByStatut(StatutChallenge statut);
}
