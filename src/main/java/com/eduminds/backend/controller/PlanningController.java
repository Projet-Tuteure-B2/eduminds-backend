package com.eduminds.backend.controller;

import com.eduminds.backend.entity.*;
import com.eduminds.backend.repository.*;
import com.eduminds.backend.service.PlanningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;
    private final UserRepository userRepository;
    private final ExamenRepository examenRepository;
    private final MatiereRepository matiereRepository;

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    @GetMapping
    public ResponseEntity<Planning> getPlanning(@AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        Planning planning = planningService.getPlanningUser(user.getId());
        if (planning == null) {
            planning = planningService.genererPlanning(user);
        }
        return ResponseEntity.ok(planning);
    }

    @GetMapping("/semaine")
    public ResponseEntity<List<SessionPlanifiee>> getSemaine(
            @AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        Planning planning = planningService.getPlanningUser(user.getId());
        if (planning == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(planningService.getSessionsSemaine(planning.getId()));
    }

    @PostMapping("/examens")
    public ResponseEntity<Examen> ajouterExamen(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Map<String, Object> body) {

        User user = getUser(ud);
        String titre        = (String) body.get("titre");
        LocalDate date      = LocalDate.parse((String) body.get("dateExamen"));
        ImportanceExamen imp = ImportanceExamen.valueOf(
                (String) body.getOrDefault("importance", "NORMALE"));
        Long matiereId      = body.containsKey("matiereId")
                ? Long.parseLong(body.get("matiereId").toString()) : null;

        Examen examen = planningService.ajouterExamen(user, titre, date, imp, matiereId, matiereRepository);
        return ResponseEntity.ok(examen);
    }

    @GetMapping("/examens")
    public ResponseEntity<List<Examen>> getExamens(@AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        return ResponseEntity.ok(
                examenRepository.findByUserIdAndDateExamenAfterOrderByDateExamenAsc(
                        user.getId(), LocalDate.now()));
    }

    @PostMapping("/recalculer")
    public ResponseEntity<Planning> recalculer(@AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        return ResponseEntity.ok(planningService.recalculer(user));
    }
}
