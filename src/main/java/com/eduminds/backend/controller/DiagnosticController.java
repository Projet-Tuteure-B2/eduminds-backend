package com.eduminds.backend.controller;

import com.eduminds.backend.entity.*;
import com.eduminds.backend.repository.UserRepository;
import com.eduminds.backend.service.DiagnosticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
@RequiredArgsConstructor
public class DiagnosticController {

    private final DiagnosticService diagnosticService;
    private final UserRepository userRepository;

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    /**
     * Démarrer le diagnostic après inscription.
     * Appelé automatiquement par le frontend dès que l'étudiant arrive sur l'écran diagnostic.
     */
    @PostMapping("/demarrer")
    public ResponseEntity<EvaluationDiagnostique> demarrer(
            @AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        EvaluationDiagnostique diag = diagnosticService.demarrer(user, ContexteDiagnostic.INSCRIPTION);
        return ResponseEntity.ok(diag);
    }

    /**
     * Récupérer le diagnostic en cours (si l'étudiant reprend après une coupure réseau).
     * Note : si l'étudiant a quitté l'app → le diagnostic a été annulé → null retourné.
     */
    @GetMapping("/en-cours")
    public ResponseEntity<EvaluationDiagnostique> getEnCours(
            @AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        return ResponseEntity.ok(diagnosticService.getDiagnosticEnCours(user.getId()));
    }

    /**
     * Soumettre la réponse à une question.
     */
    @PostMapping("/repondre")
    public ResponseEntity<ReponseDiagnostique> repondre(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Map<String, Object> body) {

        Long questionId    = Long.parseLong(body.get("questionId").toString());
        String reponse     = (String) body.get("reponseDonnee");
        Integer tempsSec   = (Integer) body.getOrDefault("tempsReponseSec", 0);

        return ResponseEntity.ok(
                diagnosticService.soumettreReponse(questionId, reponse, tempsSec));
    }

    /**
     * Terminer le diagnostic et calibrer le niveau.
     */
    @PostMapping("/{diagnosticId}/terminer")
    public ResponseEntity<Map<String, Object>> terminer(@PathVariable Long diagnosticId) {
        return ResponseEntity.ok(diagnosticService.terminer(diagnosticId));
    }

    /**
     * Annuler le diagnostic (anti-triche — appelé quand AppState passe en background).
     */
    @PostMapping("/{diagnosticId}/annuler")
    public ResponseEntity<Map<String, String>> annuler(@PathVariable Long diagnosticId) {
        diagnosticService.annuler(diagnosticId);
        return ResponseEntity.ok(Map.of(
                "message", "Diagnostic annulé. Vous devez recommencer."
        ));
    }
}
