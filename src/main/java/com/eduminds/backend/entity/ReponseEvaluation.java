package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reponses_evaluation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReponseEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reponse_donnee", columnDefinition = "TEXT")
    private String reponseDonnee;

    @Column(name = "est_correcte")
    private Boolean estCorrecte;

    // Temps réel mis pour répondre en secondes
    @Column(name = "temps_reponse_sec")
    private Integer tempsReponseSec;

    // Score de cette réponse (correct × coefficient vitesse)
    @Column(name = "score_obtenu")
    private Float scoreObtenu;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private QuestionEvaluation question;
}
