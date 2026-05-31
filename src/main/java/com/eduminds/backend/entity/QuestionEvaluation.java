package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions_evaluation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enonce;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeQuestion type;

    // Pour QCM : options séparées par "||"
    @Column(name = "options_qcm", columnDefinition = "TEXT")
    private String optionsQcm;

    @Column(name = "reponse_correcte", nullable = false, columnDefinition = "TEXT")
    private String reponseCorrecte;

    // Temps alloué en secondes (calibré par Claude selon la complexité)
    @Column(name = "temps_alloue_sec")
    private Integer tempsAlloueSec;

    // Numéro de la question dans l'évaluation (1 à 10)
    @Column(name = "numero_ordre")
    private Integer numeroOrdre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EvaluationPostRevision evaluation;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ReponseEvaluation reponse;
}
