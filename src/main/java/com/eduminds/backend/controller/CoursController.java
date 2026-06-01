package com.eduminds.backend.controller;

import com.eduminds.backend.entity.*;
import com.eduminds.backend.repository.*;
import com.eduminds.backend.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class CoursController {

    private final ImportService importService;
    private final UserRepository userRepository;
    private final MatiereRepository matiereRepository;
    private final ChapitreRepository chapitreRepository;
    private final FlashcardRepository flashcardRepository;

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // ── Import texte brut ──────────────────────────────────────────────────

    @PostMapping("/import/texte")
    public ResponseEntity<Chapitre> importerTexte(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Map<String, Object> body) {

        User user      = getUser(ud);
        String texte   = (String) body.get("texte");
        String titre   = (String) body.getOrDefault("titre", "Nouveau chapitre");
        Long matiereId = Long.parseLong(body.get("matiereId").toString());

        Chapitre chapitre = importService.importerTexte(texte, titre, matiereId, user);
        return ResponseEntity.ok(chapitre);
    }

    // ── Import fichiers (PDF / photos) ────────────────────────────────────

    @PostMapping("/import/fichiers")
    public ResponseEntity<List<Chapitre>> importerFichiers(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam("fichiers") List<MultipartFile> fichiers,
            @RequestParam("matiereId") Long matiereId,
            @RequestParam(value = "titres", required = false) List<String> titres) throws Exception {

        if (fichiers.size() > 5) {
            return ResponseEntity.badRequest().build();
        }

        User user = getUser(ud);
        List<Chapitre> chapitres = importService.importerFichiers(fichiers, matiereId, titres, user);
        return ResponseEntity.ok(chapitres);
    }

    // ── Proposition de découpage (multi-fichiers) ────────────────────────

    @PostMapping("/import/decoupage")
    public ResponseEntity<String> proposerDecoupage(@RequestBody Map<String, List<String>> body) {
        List<String> contenus = body.get("contenus");
        return ResponseEntity.ok(importService.proposerDecoupage(contenus));
    }

    // ── Liste des matières de l'utilisateur ──────────────────────────────

    @GetMapping("/matieres")
    public ResponseEntity<List<Matiere>> getMatieres(@AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        return ResponseEntity.ok(matiereRepository.findByUserId(user.getId()));
    }

    // ── Créer une matière custom ──────────────────────────────────────────

    @PostMapping("/matieres")
    public ResponseEntity<Matiere> creerMatiere(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Map<String, String> body) {

        User user = getUser(ud);
        Matiere matiere = Matiere.builder()
                .nom(body.get("nom"))
                .couleur(body.getOrDefault("couleur", "#6366F1"))
                .estCustom(true)
                .user(user)
                .build();
        return ResponseEntity.ok(matiereRepository.save(matiere));
    }

    // ── Chapitres d'une matière ───────────────────────────────────────────

    @GetMapping("/matieres/{matiereId}/chapitres")
    public ResponseEntity<List<Chapitre>> getChapitres(@PathVariable Long matiereId) {
        return ResponseEntity.ok(chapitreRepository.findByMatiereId(matiereId));
    }

    // ── Flashcards d'un chapitre ──────────────────────────────────────────

    @GetMapping("/chapitres/{chapitreId}/flashcards")
    public ResponseEntity<List<Flashcard>> getFlashcards(@PathVariable Long chapitreId) {
        return ResponseEntity.ok(flashcardRepository.findByChapitreId(chapitreId));
    }

    // ── Supprimer un cours (cascade) ──────────────────────────────────────

    @DeleteMapping("/chapitres/{chapitreId}")
    public ResponseEntity<Map<String, String>> supprimerChapitre(@PathVariable Long chapitreId) {
        chapitreRepository.deleteById(chapitreId);
        return ResponseEntity.ok(Map.of("message", "Chapitre supprimé avec toutes ses flashcards et progressions"));
    }
}
