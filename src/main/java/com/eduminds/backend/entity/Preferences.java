package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Preferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Toggle "Suggestions IA pour mes réponses ouvertes" (activé par défaut)
    @Column(name = "ia_suggestions_qro")
    @Builder.Default
    private Boolean iaSuggestionsQro = true;

    // Langue de l'interface (fr par défaut)
    @Builder.Default
    private String langue = "fr";

    // Notifications push activées
    @Column(name = "notifs_push")
    @Builder.Default
    private Boolean notifsPush = true;

    // Notifications in-app challenges
    @Column(name = "notifs_challenge")
    @Builder.Default
    private Boolean notifsChallenge = true;
}
