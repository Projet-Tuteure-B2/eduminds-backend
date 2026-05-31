package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reponses_challenge")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReponseChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reponse_donnee", columnDefinition = "TEXT")
    private String reponseDonnee;

    @Column(name = "est_correcte")
    private Boolean estCorrecte;

    // Temps mis en millisecondes (précision pour le score Kahoot)
    @Column(name = "temps_reponse_ms")
    private Long tempsReponseMs;

    // Points obtenus (score Kahoot = pointsMax × (1 - tempsMs / (tempsAlloue × 1000)))
    @Column(name = "points_obtenus")
    @Builder.Default
    private Integer pointsObtenus = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private QuestionChallenge question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ParticipationChallenge participant;
}
