package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluations_carte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationCarte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Niveau choisi par l'étudiant (auto-évaluation pour QRO après suggestion IA)
    @Enumerated(EnumType.STRING)
    private NiveauReponse niveauChoisi;

    // Temps de réponse en secondes
    @Column(name = "temps_reponse_sec")
    private Integer tempsReponseSec;

    // Réponse tapée par l'étudiant (QRO uniquement)
    @Column(name = "reponse_etudiante", columnDefinition = "TEXT")
    private String reponseEtudiante;

    // Score IA de similarité sémantique (0.0 à 1.0, QRO uniquement)
    @Column(name = "score_ia")
    private Float scoreIa;

    // Feedback textuel généré par Claude (QRO uniquement)
    @Column(name = "feedback_ia", columnDefinition = "TEXT")
    private String feedbackIa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SessionRevision session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Flashcard flashcard;
}
