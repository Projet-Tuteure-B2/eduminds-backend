package com.eduminds.backend.controller;

import com.eduminds.backend.entity.*;
import com.eduminds.backend.repository.*;
import com.eduminds.backend.service.LeitnerService;
import com.eduminds.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/revision")
@RequiredArgsConstructor
public class RevisionController {

    private final LeitnerService leitnerService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final SessionRevisionRepository sessionRepo;
    private final ProgressionCarteRepository progressionRepo;
    private final EvaluationCarteRepository evalCarteRepo;
    private final JourneeRevisionRepository journeeRepo;

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // ── Cartes à réviser aujourd'hui ──────────────────────────────────────

    @GetMapping("/cartes-du-jour")
    public ResponseEntity<List<ProgressionCarte>> getCartesDuJour(
            @AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        return ResponseEntity.ok(leitnerService.getCartesAReviser(user.getId()));
    }

    // ── Démarrer une session de révision ─────────────────────────────────

    @PostMapping("/session/demarrer")
    public ResponseEntity<SessionRevision> demarrerSession(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Map<String, Object> body) {

        User user = getUser(ud);
        Long chapitreId = body.containsKey("chapitreId")
                ? Long.parseLong(body.get("chapitreId").toString()) : null;

        SessionRevision session = SessionRevision.builder()
                .user(user)
                .dateDebut(LocalDateTime.now())
                .terminee(false)
                .nbCartesTraitees(0)
                .build();

        return ResponseEntity.ok(sessionRepo.save(session));
    }

    // ── Évaluer une carte pendant la session ─────────────────────────────

    @PostMapping("/session/{sessionId}/evaluer")
    public ResponseEntity<Map<String, Object>> evaluerCarte(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> body) {

        Long progressionId   = Long.parseLong(body.get("progressionId").toString());
        Long flashcardId     = Long.parseLong(body.get("flashcardId").toString());
        String niveauStr     = (String) body.get("niveau");
        Integer tempsSec     = (Integer) body.getOrDefault("tempsReponseSec", 0);
        String reponseEtud   = (String) body.getOrDefault("reponseEtudiante", null);

        NiveauReponse niveau = NiveauReponse.valueOf(niveauStr);

        // Mettre à jour la progression Leitner
        ProgressionCarte prog = leitnerService.appliquerResultat(progressionId, niveau);

        // Enregistrer l'évaluation
        SessionRevision session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        EvaluationCarte eval = EvaluationCarte.builder()
                .session(session)
                .flashcard(prog.getFlashcard())
                .niveauChoisi(niveau)
                .tempsReponseSec(tempsSec)
                .reponseEtudiante(reponseEtud)
                .build();
        evalCarteRepo.save(eval);

        // Incrémenter le compteur de cartes
        session.setNbCartesTraitees(session.getNbCartesTraitees() + 1);
        sessionRepo.save(session);

        return ResponseEntity.ok(Map.of(
                "nouvelleBoite", prog.getBoite(),
                "prochaineRevision", prog.getProchaineRevision().toString()
        ));
    }

    // ── Terminer une session ──────────────────────────────────────────────

    @PostMapping("/session/{sessionId}/terminer")
    public ResponseEntity<Map<String, Object>> terminerSession(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long sessionId) {

        User user = getUser(ud);
        SessionRevision session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        session.setTerminee(true);
        session.setDateFin(LocalDateTime.now());
        sessionRepo.save(session);

        // Mettre à jour la heatmap
        leitnerService.enregistrerRevisionDuJour(user, session.getNbCartesTraitees());

        // Notification de félicitations si bonne session
        if (session.getNbCartesTraitees() >= 10) {
            notificationService.creer(user, TypeNotification.FELICITATIONS,
                    Map.of("nbCartes", String.valueOf(session.getNbCartesTraitees())));
        }

        return ResponseEntity.ok(Map.of(
                "nbCartesTraitees", session.getNbCartesTraitees(),
                "message", "Session terminée avec succès !"
        ));
    }

    // ── Abandonner une session (anti-triche) ─────────────────────────────

    @PostMapping("/session/{sessionId}/abandonner")
    public ResponseEntity<Map<String, String>> abandonnerSession(@PathVariable Long sessionId) {
        sessionRepo.findById(sessionId).ifPresent(s -> {
            s.setTerminee(false);
            sessionRepo.save(s);
        });
        return ResponseEntity.ok(Map.of("message", "Session abandonnée"));
    }

    // ── Heatmap ───────────────────────────────────────────────────────────

    @GetMapping("/heatmap")
    public ResponseEntity<List<JourneeRevision>> getHeatmap(
            @AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        java.time.LocalDate debut = java.time.LocalDate.now().minusMonths(12);
        java.time.LocalDate fin   = java.time.LocalDate.now();
        return ResponseEntity.ok(
                journeeRepo.findByUserIdAndDateBetweenOrderByDate(user.getId(), debut, fin));
    }

    // ── Dashboard stats ───────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        return ResponseEntity.ok(Map.of(
                "xpTotal",     user.getXpTotal(),
                "streak",      user.getStreak(),
                "boite1",      leitnerService.getNbCartesParBoite(user.getId(), 1),
                "boite2",      leitnerService.getNbCartesParBoite(user.getId(), 2),
                "boite3",      leitnerService.getNbCartesParBoite(user.getId(), 3),
                "boite4",      leitnerService.getNbCartesParBoite(user.getId(), 4),
                "boite5",      leitnerService.getNbCartesParBoite(user.getId(), 5),
                "maitrisees",  leitnerService.getNbCartesParBoite(user.getId(), 6)
        ));
    }
}
