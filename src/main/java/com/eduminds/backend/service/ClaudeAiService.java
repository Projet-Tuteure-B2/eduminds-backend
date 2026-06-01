package com.eduminds.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Service central d'appel à l'API Claude (Anthropic).
 *
 * MODE MOCK : si claude.api.key est vide, toutes les méthodes retournent
 * des données fictives cohérentes. Dès que tu définis la variable d'env
 * CLAUDE_API_KEY, le vrai appel s'active automatiquement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeAiService {

    @Value("${claude.api.key:}")
    private String apiKey;

    @Value("${claude.api.model.default:claude-haiku-4-5-20251001}")
    private String modelDefault;

    @Value("${claude.api.model.diagnostic:claude-sonnet-4-6}")
    private String modelDiagnostic;

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Génération de flashcards à partir d'un texte de cours
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Génère des flashcards (QCM, QRO, Vrai/Faux) à partir du contenu d'un cours.
     * Retourne un JSON array de flashcards.
     */
    public String genererFlashcards(String contenuCours, String nomMatiere) {
        if (isMockMode()) {
            return mockFlashcards(nomMatiere);
        }

        String prompt = """
            Tu es un expert pédagogique. À partir du cours suivant sur "%s",
            génère exactement 10 flashcards variées (mélange de QCM, QRO et Vrai/Faux).
            
            COURS :
            %s
            
            Réponds UNIQUEMENT en JSON valide, sans markdown, sans texte avant ou après.
            Format attendu :
            [
              {
                "question": "...",
                "reponseReference": "...",
                "type": "QCM|QRO|VRAI_FAUX",
                "optionsQcm": "Option A||Option B||Option C||Option D",
                "difficulte": 1
              }
            ]
            Pour QRO et VRAI_FAUX, optionsQcm doit être null.
            Pour VRAI_FAUX, reponseReference doit être "Vrai" ou "Faux".
            difficulte : 1=facile, 2=moyen, 3=difficile.
            """.formatted(nomMatiere, contenuCours);

        return appelClaude(prompt, modelDefault);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Proposition de découpage en chapitres (multi-fichiers)
    // ─────────────────────────────────────────────────────────────────────────

    public String proposerDecoupageChapitres(List<String> contenusFichiers) {
        if (isMockMode()) {
            return mockDecoupageChapitres(contenusFichiers.size());
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contenusFichiers.size(); i++) {
            sb.append("--- Fichier %d ---\n".formatted(i + 1));
            sb.append(contenusFichiers.get(i)).append("\n\n");
        }

        String prompt = """
            Analyse les %d fichiers de cours ci-dessous et propose un découpage en chapitres.
            
            %s
            
            Réponds UNIQUEMENT en JSON valide sans markdown :
            [
              {
                "titre": "Titre du chapitre",
                "indices_fichiers": [0, 1],
                "description": "Brève description du contenu"
              }
            ]
            indices_fichiers = indices (0-based) des fichiers qui composent ce chapitre.
            """.formatted(contenusFichiers.size(), sb.toString());

        return appelClaude(prompt, modelDefault);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Suggestion de matière à partir du contenu
    // ─────────────────────────────────────────────────────────────────────────

    public String suggererMatiere(String contenuCours, List<String> matieresDisponibles) {
        if (isMockMode()) {
            return matieresDisponibles.isEmpty() ? "Matière inconnue" : matieresDisponibles.get(0);
        }

        String prompt = """
            Lis le début de ce cours et identifie à quelle matière il appartient
            parmi cette liste : %s
            
            Cours (extrait) :
            %s
            
            Réponds UNIQUEMENT avec le nom exact de la matière de la liste, rien d'autre.
            """.formatted(String.join(", ", matieresDisponibles), contenuCours.substring(0, Math.min(500, contenuCours.length())));

        return appelClaude(prompt, modelDefault).trim();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Génération de questions d'évaluation post-révision (10 questions)
    // ─────────────────────────────────────────────────────────────────────────

    public String genererQuestionsPostRevision(String contenuCours, List<String> questionsDejaVues) {
        if (isMockMode()) {
            return mockQuestionsEvaluation(10);
        }

        String prompt = """
            Tu es un évaluateur pédagogique. Génère 10 questions d'évaluation
            sur ce cours. Ces questions doivent être DIFFÉRENTES de celles déjà vues : %s
            
            COURS :
            %s
            
            Réponds UNIQUEMENT en JSON valide sans markdown :
            [
              {
                "enonce": "...",
                "type": "QCM|QRO|VRAI_FAUX",
                "optionsQcm": "A||B||C||D",
                "reponseCorrecte": "...",
                "tempsAlloueSec": 20,
                "numeroOrdre": 1
              }
            ]
            tempsAlloueSec : entre 10 (question courte) et 45 (question complexe).
            Varie les types. Pour QRO et VRAI_FAUX, optionsQcm = null.
            """.formatted(String.join("; ", questionsDejaVues), contenuCours);

        return appelClaude(prompt, modelDefault);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Évaluation sémantique d'une réponse ouverte (QRO)
    // ─────────────────────────────────────────────────────────────────────────

    public String evaluerReponseQRO(String question, String reponseReference, String reponseEtudiant) {
        if (isMockMode()) {
            return """
                {"score": 0.75, "feedback": "Bonne compréhension générale. Tu as identifié les points clés mais tu aurais pu préciser davantage le contexte.", "niveauSuggere": "CORRECT"}
                """;
        }

        String prompt = """
            Évalue la réponse d'un étudiant à cette question de cours.
            
            Question : %s
            Réponse de référence : %s
            Réponse de l'étudiant : %s
            
            Réponds UNIQUEMENT en JSON valide sans markdown :
            {
              "score": 0.75,
              "feedback": "Message court et constructif (max 2 phrases) expliquant ce qui est juste et ce qui manque.",
              "niveauSuggere": "A_REVOIR|DIFFICILE|CORRECT|FACILE"
            }
            score : de 0.0 à 1.0 (similarité sémantique).
            niveauSuggere : A_REVOIR si score < 0.30, DIFFICILE si 0.30-0.65, CORRECT si 0.65-0.90, FACILE si > 0.90.
            """.formatted(question, reponseReference, reponseEtudiant);

        return appelClaude(prompt, modelDefault);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Génération de questions diagnostiques
    // ─────────────────────────────────────────────────────────────────────────

    public String genererQuestionsDiagnostic(String filiere, String niveau) {
        if (isMockMode()) {
            return mockQuestionsEvaluation(15);
        }

        String prompt = """
            Génère 15 questions diagnostiques pour un étudiant en %s de niveau %s.
            Les questions doivent couvrir les bases fondamentales de cette filière
            et permettre d'évaluer le niveau réel de l'étudiant.
            
            Réponds UNIQUEMENT en JSON valide sans markdown :
            [
              {
                "enonce": "...",
                "type": "QCM|QRO|VRAI_FAUX",
                "optionsQcm": "A||B||C||D",
                "reponseCorrecte": "...",
                "tempsAlloueSec": 25,
                "numeroOrdre": 1
              }
            ]
            Varie les types et les difficultés. Commence par des questions simples.
            """.formatted(filiere, niveau);

        return appelClaude(prompt, modelDiagnostic);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. Calibrage du niveau après diagnostic
    // ─────────────────────────────────────────────────────────────────────────

    public String calibrerNiveau(String filiere, String niveauDeclare,
                                  int nbCorrect, int nbTotal, double tempsMoyen) {
        if (isMockMode()) {
            return """
                {"niveauCalibre": "Intermédiaire", "description": "Tu as de bonnes bases. Concentre-toi sur les concepts avancés.", "recommandations": ["Revoir les fondamentaux", "Pratiquer avec des exercices variés"]}
                """;
        }

        String prompt = """
            Un étudiant en %s (niveau déclaré : %s) vient de passer un diagnostic.
            Résultats : %d/%d bonnes réponses, temps moyen de réponse : %.1f secondes.
            
            Calibre son niveau réel et donne des recommandations.
            
            Réponds UNIQUEMENT en JSON valide sans markdown :
            {
              "niveauCalibre": "Débutant|Intermédiaire|Avancé|Expert",
              "description": "Message personnalisé d'encouragement (2 phrases max)",
              "recommandations": ["conseil 1", "conseil 2", "conseil 3"]
            }
            """.formatted(filiere, niveauDeclare, nbCorrect, nbTotal, tempsMoyen);

        return appelClaude(prompt, modelDiagnostic);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. Génération de questions de challenge (20-35 questions)
    // ─────────────────────────────────────────────────────────────────────────

    public String genererQuestionsChallenge(String nomMatiere, String contenuCours, int nbQuestions) {
        if (isMockMode()) {
            return mockQuestionsEvaluation(nbQuestions);
        }

        String prompt = """
            Génère %d questions de challenge multijoueur sur la matière "%s".
            Les questions doivent être variées, stimulantes et adaptées à un format compétitif.
            
            Contenu de référence :
            %s
            
            Réponds UNIQUEMENT en JSON valide sans markdown :
            [
              {
                "enonce": "...",
                "type": "QCM|QRO|VRAI_FAUX",
                "optionsQcm": "A||B||C||D",
                "reponseCorrecte": "...",
                "tempsAlloueSec": 15,
                "pointsMax": 1000,
                "numeroOrdre": 1
              }
            ]
            tempsAlloueSec : entre 10 et 20 secondes (format compétitif).
            Favorise les QCM et Vrai/Faux pour la rapidité.
            """.formatted(nbQuestions, nomMatiere, contenuCours.substring(0, Math.min(2000, contenuCours.length())));

        return appelClaude(prompt, modelDefault);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. Génération de messages de notification style Duolingo
    // ─────────────────────────────────────────────────────────────────────────

    public String genererMessageNotification(String typeNotif, String prenomEtudiant,
                                              Map<String, String> contexte) {
        if (isMockMode()) {
            return mockMessageNotification(typeNotif, prenomEtudiant);
        }

        String prompt = """
            Tu es Syn, l'assistant IA de l'app Synapz. Tu parles à %s.
            Génère un message de notification de type "%s" avec ce contexte : %s
            
            Le message doit être :
            - Court (1-2 phrases max)
            - Personnalisé avec le prénom
            - Motivant mais pas condescendant
            - Dans le style Duolingo (un peu taquin, bienveillant)
            - En français
            
            Réponds UNIQUEMENT avec le texte du message, rien d'autre.
            """.formatted(prenomEtudiant, typeNotif, contexte.toString());

        return appelClaude(prompt, modelDefault).trim();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Appel HTTP à l'API Claude
    // ─────────────────────────────────────────────────────────────────────────

    private String appelClaude(String prompt, String model) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "max_tokens", 4096,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", API_VERSION)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Claude API error {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("Erreur API Claude : " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("content").get(0).path("text").asText();

        } catch (Exception e) {
            log.error("Erreur appel Claude API : {}", e.getMessage());
            throw new RuntimeException("Impossible de contacter l'IA : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mode mock — données fictives cohérentes
    // ─────────────────────────────────────────────────────────────────────────

    private boolean isMockMode() {
        boolean mock = apiKey == null || apiKey.isBlank();
        if (mock) log.debug("🤖 Claude en mode MOCK (pas de clé API)");
        return mock;
    }

    private String mockFlashcards(String matiere) {
        return """
            [
              {"question": "Quelle est la définition de base de %s ?", "reponseReference": "C'est un concept fondamental qui...", "type": "QRO", "optionsQcm": null, "difficulte": 1},
              {"question": "Le principe A est-il vrai ?", "reponseReference": "Vrai", "type": "VRAI_FAUX", "optionsQcm": null, "difficulte": 1},
              {"question": "Parmi ces propositions, laquelle est correcte ?", "reponseReference": "Option B", "type": "QCM", "optionsQcm": "Option A||Option B||Option C||Option D", "difficulte": 2},
              {"question": "Expliquez le mécanisme principal de %s.", "reponseReference": "Le mécanisme principal consiste à...", "type": "QRO", "optionsQcm": null, "difficulte": 2},
              {"question": "Quelle est la formule de base ?", "reponseReference": "Option C", "type": "QCM", "optionsQcm": "Formule A||Formule B||Formule C||Formule D", "difficulte": 2},
              {"question": "Ce concept s'applique uniquement en théorie.", "reponseReference": "Faux", "type": "VRAI_FAUX", "optionsQcm": null, "difficulte": 1},
              {"question": "Quels sont les 3 éléments clés ?", "reponseReference": "Les 3 éléments sont : 1) ... 2) ... 3) ...", "type": "QRO", "optionsQcm": null, "difficulte": 3},
              {"question": "Quel est l'auteur principal de cette théorie ?", "reponseReference": "Option A", "type": "QCM", "optionsQcm": "Auteur A||Auteur B||Auteur C||Auteur D", "difficulte": 1},
              {"question": "La méthode B est plus efficace que la méthode A.", "reponseReference": "Vrai", "type": "VRAI_FAUX", "optionsQcm": null, "difficulte": 2},
              {"question": "Décrivez les limites de cette approche.", "reponseReference": "Les limites principales sont...", "type": "QRO", "optionsQcm": null, "difficulte": 3}
            ]
            """.formatted(matiere, matiere);
    }

    private String mockDecoupageChapitres(int nbFichiers) {
        if (nbFichiers == 1) {
            return """
                [{"titre": "Chapitre 1 — Introduction", "indices_fichiers": [0], "description": "Contenu du fichier importé"}]
                """;
        }
        return """
            [
              {"titre": "Chapitre 1 — Partie théorique", "indices_fichiers": [0], "description": "Concepts fondamentaux"},
              {"titre": "Chapitre 2 — Applications pratiques", "indices_fichiers": [1], "description": "Exercices et cas pratiques"}
            ]
            """;
    }

    private String mockQuestionsEvaluation(int nb) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 1; i <= nb; i++) {
            if (i > 1) sb.append(",");
            sb.append("""
                {"enonce": "Question %d : Quel est le principe fondamental ?", "type": "QCM", "optionsQcm": "Réponse A||Réponse B||Réponse C||Réponse D", "reponseCorrecte": "Réponse B", "tempsAlloueSec": 20, "numeroOrdre": %d}
                """.formatted(i, i));
        }
        sb.append("]");
        return sb.toString();
    }

    private String mockMessageNotification(String type, String prenom) {
        return switch (type) {
            case "RAPPEL_REVISION"   -> "%s, tu as %d cartes qui t'attendent ! 🧠 Elles ne vont pas se réviser toutes seules.".formatted(prenom, 12);
            case "STREAK_EN_DANGER"  -> "⚠️ %s ! Ton streak de 7 jours va sauter si tu ne révises pas aujourd'hui !".formatted(prenom);
            case "EXAMEN_PROCHE"     -> "%s, ton examen approche ! 📅 Plus que quelques jours pour consolider tes révisions.".formatted(prenom);
            case "FELICITATIONS"     -> "Bravo %s ! 🎉 Tu viens de maîtriser un nouveau chapitre. Continue comme ça !".formatted(prenom);
            default                  -> "%s, Syn t'attend pour une nouvelle session ! 🚀".formatted(prenom);
        };
    }
}
