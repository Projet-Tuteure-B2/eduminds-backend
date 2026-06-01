package com.eduminds.backend.service;

import com.eduminds.backend.entity.*;
import com.eduminds.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notifRepo;
    private final ClaudeAiService claudeAi;

    // ─────────────────────────────────────────────────────────────────────────
    // Créer une notification
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Notification creer(User user, TypeNotification type, Map<String, String> contexte) {
        String message = claudeAi.genererMessageNotification(
                type.name(), user.getName().split(" ")[0], contexte);

        Notification notif = Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .estLue(false)
                .build();

        return notifRepo.save(notif);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Créer notification challenge (in-app)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Notification creerNotifChallenge(User user, TypeNotification type,
                                             Long salleId, String messageFixe) {
        Notification notif = Notification.builder()
                .user(user)
                .type(type)
                .message(messageFixe)
                .estLue(false)
                .salleIdRef(salleId)
                .build();

        return notifRepo.save(notif);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Récupérer les notifications d'un utilisateur
    // ─────────────────────────────────────────────────────────────────────────

    public List<Notification> getNotifications(Long userId) {
        return notifRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getNonLues(Long userId) {
        return notifRepo.findByUserIdAndEstLueFalseOrderByCreatedAtDesc(userId);
    }

    public long getNbNonLues(Long userId) {
        return notifRepo.countByUserIdAndEstLueFalse(userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Marquer comme lue
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void marquerLue(Long notifId) {
        notifRepo.findById(notifId).ifPresent(n -> {
            n.setEstLue(true);
            notifRepo.save(n);
        });
    }

    @Transactional
    public void marquerToutesLues(Long userId) {
        notifRepo.findByUserIdAndEstLueFalseOrderByCreatedAtDesc(userId)
                .forEach(n -> {
                    n.setEstLue(true);
                    notifRepo.save(n);
                });
    }
}
