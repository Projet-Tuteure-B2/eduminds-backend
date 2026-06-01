package com.eduminds.backend.controller;

import com.eduminds.backend.entity.*;
import com.eduminds.backend.repository.UserRepository;
import com.eduminds.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAll(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(notificationService.getNotifications(getUser(ud).getId()));
    }

    @GetMapping("/non-lues")
    public ResponseEntity<Map<String, Object>> getNonLues(@AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        return ResponseEntity.ok(Map.of(
                "notifications", notificationService.getNonLues(user.getId()),
                "count",         notificationService.getNbNonLues(user.getId())
        ));
    }

    @PutMapping("/{id}/lire")
    public ResponseEntity<Map<String, String>> marquerLue(@PathVariable Long id) {
        notificationService.marquerLue(id);
        return ResponseEntity.ok(Map.of("message", "Notification marquée comme lue"));
    }

    @PutMapping("/lire-tout")
    public ResponseEntity<Map<String, String>> marquerToutesLues(
            @AuthenticationPrincipal UserDetails ud) {
        notificationService.marquerToutesLues(getUser(ud).getId());
        return ResponseEntity.ok(Map.of("message", "Toutes les notifications marquées comme lues"));
    }
}
