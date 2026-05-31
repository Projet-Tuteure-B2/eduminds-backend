package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Identité ──────────────────────────────────────────────
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;           // Toujours stocké hashé via BCrypt

    @Column(name = "avatar_url")
    private String avatarUrl;

    // ── Scolarité ─────────────────────────────────────────────
    private String filiere;            // ex. "Droit", "Sciences"
    private String level;              // ex. "L2", "BTS1"

    @Column(name = "niveau_calibre")
    private String niveauCalibre;      // Niveau réel déterminé après diagnostic

    // ── Progression ───────────────────────────────────────────
    @Column(name = "xp_total")
    @Builder.Default
    private Integer xpTotal = 0;

    @Builder.Default
    private Integer streak = 0;

    // ── Auth & Sécurité ───────────────────────────────────────
    @Column(name = "diagnostic_passed")
    @Builder.Default
    private Boolean diagnosticPassed = false;  // Inscription validée seulement si true

    @Column(name = "token_version")
    @Builder.Default
    private Integer tokenVersion = 0;           // Incrémenté à chaque logout pour invalider les tokens

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // ── Timestamps ────────────────────────────────────────────
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (xpTotal == null)        xpTotal = 0;
        if (streak == null)         streak = 0;
        if (tokenVersion == null)   tokenVersion = 0;
        if (diagnosticPassed == null) diagnosticPassed = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
