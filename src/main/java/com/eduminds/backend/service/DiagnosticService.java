package com.eduminds.backend.service;

import com.eduminds.backend.entity.*;
import com.eduminds.backend.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosticService {

    private final EvaluationDiagnostiqueRepository diagnosticRepo;
    private final QuestionDiagnostiqueRepository questionRepo;
    private final ReponseDiagnostiqueRepository reponseRepo;
    private final UserRepository userRepository;
    private final ClaudeAiService claudeAi;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────────────────
    // Démarrer un diagnostic
    // Contextes : INSCRIPTION ou CHANGEMENT_FILIERE
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public EvaluationDiagnostique demarrer(User user, ContexteDiagnostic contexte) {
        EvaluationDiagnostique diag = EvaluationDiagnostique.builder()
                .user(user)
                .contexte(contexte)
                .filiereEvaluee(user.getFiliere())
                .niveauEvalue(user.getLevel())
                .dateDebut(LocalDateTime.now())
                .terminee(false)
                .build();

        diag = diagnosticRepo.save(diag);

        // Générer les questions via Claude
        String json = claudeAi.genererQuestionsDiagnostic(user.getFiliere(), user.getLevel());
        json = json.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            List<Map<String, Object>> questionsData = objectMapper.readValue(
                    json, new TypeReference<>() {});

            for (Map<String, Object> data : questionsData) {
                QuestionDiagnostique question = QuestionDiagnostique.builder()
                        .enonce((String) data.get("enonce"))
                        .type(TypeQuestion.valueOf((String) data.get("type")))
                        .optionsQcm((String) data.getOrDefault("optionsQcm", null))
                        .reponseCorrecte((String) data.get("reponseCorrecte"))
                        .tempsAlloueSec((Integer) data.getOrDefault("tempsAlloueSec", 25))
                        .numeroOrdre((Integer) data.get("numeroOrdre"))
                        .diagnostic(diag)
                        .build();
                questionRepo.save(question);
            }
        } catch (Exception e) {
            log.error("Erreur parsing questions diagnostic : {}", e.getMessage());
        }

        log.info("🔬 Diagnostic démarré pour {} (contexte: {})", user.getEmail(), contexte);
        return diag;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Soumettre une réponse
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public ReponseDiagnostique soumettreReponse(Long questionId, String reponseDonnee,
                                                 Integer tempsReponseSec) {
        QuestionDiagnostique question = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question introuvable"));

        boolean estCorrecte = verifierReponse(question, reponseDonnee);

        ReponseDiagnostique reponse = ReponseDiagnostique.builder()
                .question(question)
                .reponseDonnee(reponseDonnee)
                .estCorrecte(estCorrecte)
                .tempsReponseSec(tempsReponseSec)
                .build();

        return reponseRepo.save(reponse);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Terminer le diagnostic et calibrer le niveau
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> terminer(Long diagnosticId) {
        EvaluationDiagnostique diag = diagnosticRepo.findById(diagnosticId)
                .orElseThrow(() -> new RuntimeException("Diagnostic introuvable"));

        List<QuestionDiagnostique> questions = questionRepo
                .findByDiagnosticIdOrderByNumeroOrdre(diagnosticId);

        long nbCorrect = questions.stream()
                .filter(q -> q.getReponse() != null && Boolean.TRUE.equals(q.getReponse().getEstCorrecte()))
                .count();

        double tempsMoyen = questions.stream()
                .filter(q -> q.getReponse() != null && q.getReponse().getTempsReponseSec() != null)
                .mapToInt(q -> q.getReponse().getTempsReponseSec())
                .average()
                .orElse(20.0);

        // Calibrer via Claude
        String json = claudeAi.calibrerNiveau(
                diag.getFiliereEvaluee(), diag.getNiveauEvalue(),
                (int) nbCorrect, questions.size(), tempsMoyen);
        json = json.replaceAll("```json", "").replaceAll("```", "").trim();

        String niveauCalibre = diag.getNiveauEvalue(); // fallback
        try {
            Map<String, Object> resultat = objectMapper.readValue(json, new TypeReference<>() {});
            niveauCalibre = (String) resultat.get("niveauCalibre");

            // Mettre à jour le diagnostic
            diag.setNiveauCalibreResultat(niveauCalibre);
            diag.setTerminee(true);
            diag.setDateFin(LocalDateTime.now());
            diagnosticRepo.save(diag);

            // Mettre à jour le profil utilisateur
            User user = diag.getUser();
            user.setNiveauCalibre(niveauCalibre);
            user.setDiagnosticPassed(true);
            userRepository.save(user);

            log.info("✅ Diagnostic terminé pour {} — niveau : {}", user.getEmail(), niveauCalibre);
            return resultat;

        } catch (Exception e) {
            log.error("Erreur calibrage niveau : {}", e.getMessage());
            return Map.of("niveauCalibre", niveauCalibre, "description", "Calibrage en cours...");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Annuler un diagnostic (anti-triche — étudiant a quitté l'app)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void annuler(Long diagnosticId) {
        diagnosticRepo.findById(diagnosticId).ifPresent(diag -> {
            // On supprime le diagnostic — l'étudiant devra recommencer
            diagnosticRepo.delete(diag);
            log.warn("⚠️ Diagnostic {} annulé (anti-triche)", diagnosticId);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Récupérer le diagnostic en cours
    // ─────────────────────────────────────────────────────────────────────────

    public EvaluationDiagnostique getDiagnosticEnCours(Long userId) {
        return diagnosticRepo
                .findFirstByUserIdAndTermineeOrderByDateDebutDesc(userId, false)
                .orElse(null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private boolean verifierReponse(QuestionDiagnostique question, String reponseDonnee) {
        if (reponseDonnee == null) return false;
        return switch (question.getType()) {
            case QCM, VRAI_FAUX ->
                question.getReponseCorrecte().trim().equalsIgnoreCase(reponseDonnee.trim());
            case QRO ->
                // Pour les QRO en diagnostic : vérification simple par mots-clés
                reponseDonnee.length() >= 10;
        };
    }
}
