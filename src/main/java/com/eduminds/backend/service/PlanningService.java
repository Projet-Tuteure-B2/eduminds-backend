package com.eduminds.backend.service;

import com.eduminds.backend.entity.*;
import com.eduminds.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanningService {

    private final PlanningRepository planningRepo;
    private final SessionPlanifieeRepository sessionPlanifieeRepo;
    private final ExamenRepository examenRepo;
    private final ChapitreRepository chapitreRepo;
    private final FlashcardRepository flashcardRepo;

    // ─────────────────────────────────────────────────────────────────────────
    // Générer ou recalculer le planning de l'utilisateur
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Planning genererPlanning(User user) {
        // Récupérer ou créer le planning
        Planning planning = planningRepo.findByUserId(user.getId())
                .orElseGet(() -> Planning.builder().user(user).build());

        // Supprimer les sessions planifiées futures non effectuées
        if (planning.getId() != null) {
            List<SessionPlanifiee> anciennes = sessionPlanifieeRepo
                    .findByPlanningIdAndStatutOrderByDatePrevueAsc(planning.getId(), StatutSession.PLANIFIEE);
            sessionPlanifieeRepo.deleteAll(anciennes);
        }

        LocalDate today = LocalDate.now();
        LocalDate dateFin = determinerDateFin(user.getId(), today);

        planning.setDateDebut(today);
        planning.setDateFin(dateFin);
        planning.setDerniereMiseAJour(today);
        planning = planningRepo.save(planning);

        // Récupérer tous les chapitres PRET de l'utilisateur
        List<Chapitre> chapitres = chapitreRepo.findAll().stream()
                .filter(c -> c.getStatut() == StatutTraitement.PRET
                        && c.getMatiere().getUser().getId().equals(user.getId()))
                .toList();

        if (chapitres.isEmpty()) {
            log.info("Aucun chapitre prêt pour {} — planning vide", user.getEmail());
            return planning;
        }

        long joursTotal = ChronoUnit.DAYS.between(today, dateFin);
        if (joursTotal <= 0) joursTotal = 30;

        // Répartir les chapitres sur la durée disponible
        long joursParChapitre = Math.max(1, joursTotal / chapitres.size());
        final Planning finalPlanning = planning;

        for (int i = 0; i < chapitres.size(); i++) {
            Chapitre chapitre = chapitres.get(i);
            long nbFlashcards = flashcardRepo.countByChapitreId(chapitre.getId());
            int nbSeances = (int) Math.max(1, nbFlashcards / 10); // ~10 cartes par séance

            for (int s = 0; s < nbSeances; s++) {
                long offset = (i * joursParChapitre) + (s * (joursParChapitre / Math.max(1, nbSeances)));
                LocalDate datePrevue = today.plusDays(offset);

                if (datePrevue.isAfter(dateFin)) break;

                SessionPlanifiee session = SessionPlanifiee.builder()
                        .datePrevue(datePrevue)
                        .nbCartesPrevues((int) Math.min(10, nbFlashcards))
                        .statut(StatutSession.PLANIFIEE)
                        .planning(finalPlanning)
                        .chapitre(chapitre)
                        .build();
                sessionPlanifieeRepo.save(session);
            }
        }

        log.info("✅ Planning généré pour {} jusqu'au {}", user.getEmail(), dateFin);
        return planning;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ajouter une échéance d'examen
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Examen ajouterExamen(User user, String titre, LocalDate dateExamen,
                                 ImportanceExamen importance, Long matiereId,
                                 MatiereRepository matiereRepo) {
        Matiere matiere = matiereId != null
                ? matiereRepo.findById(matiereId).orElse(null)
                : null;

        Examen examen = Examen.builder()
                .titre(titre)
                .dateExamen(dateExamen)
                .importance(importance)
                .matiere(matiere)
                .user(user)
                .build();

        examen = examenRepo.save(examen);

        // Recalculer le planning avec la nouvelle échéance
        genererPlanning(user);

        return examen;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Recalcul après jours manqués
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Planning recalculer(User user) {
        // Marquer les sessions passées non effectuées comme MANQUEE
        planningRepo.findByUserId(user.getId()).ifPresent(planning -> {
            List<SessionPlanifiee> sessions = sessionPlanifieeRepo
                    .findByPlanningIdAndStatutOrderByDatePrevueAsc(planning.getId(), StatutSession.PLANIFIEE);
            sessions.stream()
                    .filter(s -> s.getDatePrevue().isBefore(LocalDate.now()))
                    .forEach(s -> {
                        s.setStatut(StatutSession.MANQUEE);
                        sessionPlanifieeRepo.save(s);
                    });
        });

        return genererPlanning(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Déterminer la date de fin du planning
    // ─────────────────────────────────────────────────────────────────────────

    private LocalDate determinerDateFin(Long userId, LocalDate today) {
        // Priorité : prochain examen
        return examenRepo.findByUserIdAndDateExamenAfterOrderByDateExamenAsc(userId, today)
                .stream()
                .findFirst()
                .map(Examen::getDateExamen)
                // Pas d'examen : durée par défaut 2 mois
                .orElse(today.plusMonths(2));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Récupérer le planning
    // ─────────────────────────────────────────────────────────────────────────

    public Planning getPlanningUser(Long userId) {
        return planningRepo.findByUserId(userId).orElse(null);
    }

    public List<SessionPlanifiee> getSessionsSemaine(Long planningId) {
        LocalDate debut = LocalDate.now();
        LocalDate fin   = debut.plusDays(7);
        return sessionPlanifieeRepo
                .findByPlanningIdAndDatePrevueBetweenOrderByDatePrevueAsc(planningId, debut, fin);
    }
}
