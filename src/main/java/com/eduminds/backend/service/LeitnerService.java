package com.eduminds.backend.service;

import com.eduminds.backend.entity.*;
import com.eduminds.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implémente l'algorithme de Leitner à 6 boîtes.
 *
 * Intervalles :
 *   Boîte 1 →  1 jour
 *   Boîte 2 →  2 jours
 *   Boîte 3 →  5 jours
 *   Boîte 4 → 14 jours
 *   Boîte 5 → 30 jours
 *   Boîte 6 → 90 jours (Maîtrisée — contrôle périodique anti-oubli)
 *
 * Transitions pilotées par le score de l'évaluation post-révision :
 *   A_REVOIR  → retour boîte 1
 *   DIFFICILE → descend d'une boîte (min 1)
 *   CORRECT   → monte d'une boîte
 *   FACILE    → monte de deux boîtes (saute une boîte)
 *
 * Pour passer en boîte 6, il faut 2 réussites CORRECT/FACILE consécutives en boîte 5.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeitnerService {

    private static final int[] INTERVALLES = {0, 1, 2, 5, 14, 30, 90};

    private final ProgressionCarteRepository progressionRepo;
    private final FlashcardRepository flashcardRepo;
    private final JourneeRevisionRepository journeeRepo;

    // ─────────────────────────────────────────────────────────────────────────
    // Initialisation des cartes d'un nouveau chapitre
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void initialiserProgressionChapitre(List<Flashcard> flashcards, User user) {
        for (Flashcard card : flashcards) {
            boolean exists = progressionRepo
                    .findByFlashcardIdAndUserId(card.getId(), user.getId())
                    .isPresent();
            if (!exists) {
                ProgressionCarte prog = ProgressionCarte.builder()
                        .flashcard(card)
                        .user(user)
                        .boite(1)
                        .prochaineRevision(LocalDate.now())
                        .reussitesConsecutives(0)
                        .build();
                progressionRepo.save(prog);
            }
        }
        log.info("✅ {} progressions initialisées pour {}", flashcards.size(), user.getEmail());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Récupérer les cartes dues aujourd'hui
    // ─────────────────────────────────────────────────────────────────────────

    public List<ProgressionCarte> getCartesAReviser(Long userId) {
        return progressionRepo.findCartesAReviser(userId, LocalDate.now());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Appliquer le résultat d'une évaluation sur une carte
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public ProgressionCarte appliquerResultat(Long progressionId, NiveauReponse niveau) {
        ProgressionCarte prog = progressionRepo.findById(progressionId)
                .orElseThrow(() -> new RuntimeException("Progression introuvable : " + progressionId));

        int ancienneBoite = prog.getBoite();
        int nouvelleBoite = calculerNouvelleBoite(ancienneBoite, niveau, prog.getReussitesConsecutives());

        // Mise à jour des réussites consécutives
        if (niveau == NiveauReponse.CORRECT || niveau == NiveauReponse.FACILE) {
            prog.setReussitesConsecutives(prog.getReussitesConsecutives() + 1);
        } else {
            prog.setReussitesConsecutives(0);
        }

        prog.setBoite(nouvelleBoite);
        prog.setProchaineRevision(LocalDate.now().plusDays(INTERVALLES[nouvelleBoite]));

        log.debug("Carte {} : boîte {} → {} | prochain : {}",
                progressionId, ancienneBoite, nouvelleBoite, prog.getProchaineRevision());

        return progressionRepo.save(prog);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Calcul de la nouvelle boîte
    // ─────────────────────────────────────────────────────────────────────────

    private int calculerNouvelleBoite(int boiteActuelle, NiveauReponse niveau, int reussitesConsec) {
        return switch (niveau) {
            case A_REVOIR  -> 1;
            case DIFFICILE -> Math.max(1, boiteActuelle - 1);
            case CORRECT   -> {
                // Passage en boîte 6 (Maîtrisée) : 2 réussites consécutives en boîte 5
                if (boiteActuelle == 5 && reussitesConsec >= 1) yield 6;
                yield Math.min(5, boiteActuelle + 1);
            }
            case FACILE    -> {
                if (boiteActuelle >= 4 && reussitesConsec >= 1) yield 6;
                yield Math.min(5, boiteActuelle + 2);
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mettre à jour la heatmap du jour
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void enregistrerRevisionDuJour(User user, int nbCartes) {
        LocalDate today = LocalDate.now();
        JourneeRevision journee = journeeRepo
                .findByUserIdAndDate(user.getId(), today)
                .orElseGet(() -> JourneeRevision.builder()
                        .user(user)
                        .date(today)
                        .nbCartesRevisees(0)
                        .build());

        journee.setNbCartesRevisees(journee.getNbCartesRevisees() + nbCartes);
        journee.setIntensite(calculerIntensite(journee.getNbCartesRevisees()));
        journeeRepo.save(journee);
    }

    private int calculerIntensite(int nbCartes) {
        if (nbCartes == 0)  return 0;
        if (nbCartes < 5)   return 1;
        if (nbCartes < 15)  return 2;
        if (nbCartes < 30)  return 3;
        return 4;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Stats pour le dashboard
    // ─────────────────────────────────────────────────────────────────────────

    public long getNbCartesParBoite(Long userId, int boite) {
        return progressionRepo.countByUserIdAndBoite(userId, boite);
    }
}
