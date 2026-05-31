package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNotification type;

    // Message généré par Claude, style Duolingo (personnalisé et contextuel)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "est_lue")
    @Builder.Default
    private Boolean estLue = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Référence optionnelle vers une salle de challenge (pour les notifs challenge)
    @Column(name = "salle_id_ref")
    private Long salleIdRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
