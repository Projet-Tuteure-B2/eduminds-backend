package com.eduminds.backend.controller;

import com.eduminds.backend.config.DataSeeder;
import com.eduminds.backend.entity.User;
import com.eduminds.backend.repository.UserRepository;
import com.eduminds.backend.service.DiagnosticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profil")
@RequiredArgsConstructor
public class ProfilController {

    private final UserRepository userRepository;
    private final DiagnosticService diagnosticService;
    private final DataSeeder dataSeeder;

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    @GetMapping
    public ResponseEntity<User> getProfil(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(getUser(ud));
    }

    @PutMapping
    public ResponseEntity<User> updateProfil(@AuthenticationPrincipal UserDetails ud,
                                              @RequestBody Map<String, String> body) {
        User user = getUser(ud);
        if (body.containsKey("name"))      user.setName(body.get("name"));
        if (body.containsKey("avatarUrl")) user.setAvatarUrl(body.get("avatarUrl"));
        return ResponseEntity.ok(userRepository.save(user));
    }

    /**
     * Changer de filière → redéclenche automatiquement le diagnostic.
     */
    @PutMapping("/filiere")
    public ResponseEntity<Map<String, Object>> changerFiliere(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Map<String, String> body) {

        User user = getUser(ud);
        String nouvelleFiliere = body.get("filiere");
        String nouveauLevel    = body.getOrDefault("level", user.getLevel());

        user.setFiliere(nouvelleFiliere);
        user.setLevel(nouveauLevel);
        user.setDiagnosticPassed(false);    // Doit repasser le diagnostic
        user.setNiveauCalibre(null);
        userRepository.save(user);

        // Seed des matières pour la nouvelle filière
        dataSeeder.seedMatieresForUser(user, nouvelleFiliere);

        // Démarrer le nouveau diagnostic
        var diag = diagnosticService.demarrer(user,
                com.eduminds.backend.entity.ContexteDiagnostic.CHANGEMENT_FILIERE);

        return ResponseEntity.ok(Map.of(
                "message", "Filière mise à jour. Veuillez passer le diagnostic.",
                "diagnosticId", diag.getId()
        ));
    }
}
