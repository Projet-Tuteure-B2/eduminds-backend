package com.eduminds.backend.service;

import com.eduminds.backend.entity.*;
import com.eduminds.backend.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final ChapitreRepository chapitreRepo;
    private final FichierSourceRepository fichierSourceRepo;
    private final FlashcardRepository flashcardRepo;
    private final MatiereRepository matiereRepo;
    private final UserRepository userRepository;
    private final ClaudeAiService claudeAi;
    private final LeitnerService leitnerService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${synapz.upload.dir:uploads/cours}")
    private String uploadDir;

    // ─────────────────────────────────────────────────────────────────────────
    // Import texte brut (1 chapitre direct)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Chapitre importerTexte(String texte, String titreChap, Long matiereId, User user) {
        Matiere matiere = matiereRepo.findById(matiereId)
                .orElseThrow(() -> new RuntimeException("Matière introuvable"));

        // Créer le chapitre
        Chapitre chapitre = Chapitre.builder()
                .titre(titreChap)
                .matiere(matiere)
                .statut(StatutTraitement.EN_TRAITEMENT)
                .build();
        chapitre = chapitreRepo.save(chapitre);

        // Sauvegarder la source texte
        FichierSource source = FichierSource.builder()
                .type(TypeSource.TEXTE)
                .contenuTexte(texte)
                .nomOriginal("texte_brut.txt")
                .chapitre(chapitre)
                .build();
        fichierSourceRepo.save(source);

        // Générer les flashcards via Claude (ou mock)
        genererEtSauvegarderFlashcards(chapitre, texte, matiere.getNom(), user);

        return chapitre;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Import fichiers (PDF / photos) — jusqu'à 5 fichiers
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public List<Chapitre> importerFichiers(List<MultipartFile> fichiers,
                                            Long matiereId,
                                            List<String> titresChapitres,
                                            User user) throws IOException {

        if (fichiers.size() > 5) {
            throw new IllegalArgumentException("Maximum 5 fichiers par import");
        }

        Matiere matiere = matiereRepo.findById(matiereId)
                .orElseThrow(() -> new RuntimeException("Matière introuvable"));

        // Sauvegarder les fichiers sur le disque + extraire le texte
        List<String> contenus = new ArrayList<>();
        List<Path> chemins = new ArrayList<>();

        for (MultipartFile fichier : fichiers) {
            Path chemin = sauvegarderFichier(fichier, user.getId());
            chemins.add(chemin);
            // Pour l'instant : lire le contenu comme texte brut
            // (en Phase 4 : OCR pour images, extraction PDF pour PDF)
            String contenu = extraireTexte(fichier, chemin);
            contenus.add(contenu);
        }

        List<Chapitre> chapitres = new ArrayList<>();

        if (fichiers.size() == 1) {
            // 1 fichier → 1 chapitre direct
            String titre = (titresChapitres != null && !titresChapitres.isEmpty())
                    ? titresChapitres.get(0)
                    : fichiers.get(0).getOriginalFilename();

            Chapitre chapitre = creerChapitre(titre, matiere, StatutTraitement.EN_TRAITEMENT);
            attacher(chapitre, fichiers.get(0), chemins.get(0), contenus.get(0), TypeSource.TEXTE);
            genererEtSauvegarderFlashcards(chapitre, contenus.get(0), matiere.getNom(), user);
            chapitres.add(chapitre);

        } else {
            // Plusieurs fichiers → Claude propose un découpage (ou titres fournis par l'étudiant)
            if (titresChapitres != null && titresChapitres.size() == fichiers.size()) {
                // L'étudiant a validé un découpage 1-à-1
                for (int i = 0; i < fichiers.size(); i++) {
                    Chapitre chapitre = creerChapitre(titresChapitres.get(i), matiere, StatutTraitement.EN_TRAITEMENT);
                    attacher(chapitre, fichiers.get(i), chemins.get(i), contenus.get(i), TypeSource.TEXTE);
                    genererEtSauvegarderFlashcards(chapitre, contenus.get(i), matiere.getNom(), user);
                    chapitres.add(chapitre);
                }
            } else {
                // Retourner la proposition de découpage sans encore créer les chapitres
                // (le frontend affichera l'écran de prévisualisation)
                // On crée un seul chapitre "en attente" pour chaque fichier provisoirement
                for (int i = 0; i < fichiers.size(); i++) {
                    Chapitre chapitre = creerChapitre("Chapitre " + (i + 1), matiere, StatutTraitement.EN_TRAITEMENT);
                    attacher(chapitre, fichiers.get(i), chemins.get(i), contenus.get(i), TypeSource.TEXTE);
                    chapitres.add(chapitre);
                }
            }
        }

        return chapitres;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Proposition de découpage (appelée avant la validation de l'étudiant)
    // ─────────────────────────────────────────────────────────────────────────

    public String proposerDecoupage(List<String> contenus) {
        return claudeAi.proposerDecoupageChapitres(contenus);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Génération et sauvegarde des flashcards
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void genererEtSauvegarderFlashcards(Chapitre chapitre, String contenu,
                                                String nomMatiere, User user) {
        try {
            String json = claudeAi.genererFlashcards(contenu, nomMatiere);
            // Nettoyer les éventuels backticks markdown
            json = json.replaceAll("```json", "").replaceAll("```", "").trim();

            List<Map<String, Object>> flashcardsData = objectMapper.readValue(
                    json, new TypeReference<>() {});

            List<Flashcard> flashcards = new ArrayList<>();
            for (Map<String, Object> data : flashcardsData) {
                TypeQuestion type = TypeQuestion.valueOf((String) data.get("type"));
                Flashcard card = Flashcard.builder()
                        .question((String) data.get("question"))
                        .reponseReference((String) data.get("reponseReference"))
                        .type(type)
                        .optionsQcm((String) data.getOrDefault("optionsQcm", null))
                        .difficulte((Integer) data.getOrDefault("difficulte", 1))
                        .chapitre(chapitre)
                        .build();
                flashcards.add(flashcardRepo.save(card));
            }

            // Initialiser les progressions Leitner
            leitnerService.initialiserProgressionChapitre(flashcards, user);

            // Marquer le chapitre comme prêt
            chapitre.setStatut(StatutTraitement.PRET);
            chapitreRepo.save(chapitre);

            log.info("✅ {} flashcards générées pour le chapitre '{}'",
                    flashcards.size(), chapitre.getTitre());

        } catch (Exception e) {
            log.error("Erreur génération flashcards : {}", e.getMessage());
            chapitre.setStatut(StatutTraitement.ERREUR);
            chapitre.setMessageErreur(e.getMessage());
            chapitreRepo.save(chapitre);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers privés
    // ─────────────────────────────────────────────────────────────────────────

    private Chapitre creerChapitre(String titre, Matiere matiere, StatutTraitement statut) {
        Chapitre c = Chapitre.builder()
                .titre(titre)
                .matiere(matiere)
                .statut(statut)
                .build();
        return chapitreRepo.save(c);
    }

    private void attacher(Chapitre chapitre, MultipartFile fichier,
                          Path chemin, String contenu, TypeSource type) {
        FichierSource source = FichierSource.builder()
                .type(type)
                .cheminFichier(chemin.toString())
                .contenuTexte(contenu)
                .nomOriginal(fichier.getOriginalFilename())
                .chapitre(chapitre)
                .build();
        fichierSourceRepo.save(source);
    }

    private Path sauvegarderFichier(MultipartFile fichier, Long userId) throws IOException {
        Path dir = Paths.get(uploadDir, String.valueOf(userId));
        Files.createDirectories(dir);
        String nomFichier = UUID.randomUUID() + "_" + fichier.getOriginalFilename();
        Path destination = dir.resolve(nomFichier);
        Files.copy(fichier.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination;
    }

    private String extraireTexte(MultipartFile fichier, Path chemin) throws IOException {
        // Phase simplifiée : lecture directe du contenu texte
        // En Phase 4 : OCR pour images via Claude Vision, extraction PDF via PDFBox
        String nomFichier = fichier.getOriginalFilename() != null
                ? fichier.getOriginalFilename().toLowerCase() : "";

        if (nomFichier.endsWith(".txt") || nomFichier.endsWith(".md")) {
            return new String(fichier.getBytes());
        }

        // Pour PDF et images : placeholder (sera remplacé en Phase 4)
        return "[Contenu de " + fichier.getOriginalFilename() + " — extraction en cours]";
    }
}
