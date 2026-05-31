package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions_diagnostiques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDiagnostique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enonce;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeQuestion type;

    @Column(name = "options_qcm", columnDefinition = "TEXT")
    private String optionsQcm;

    @Column(name = "reponse_correcte", nullable = false, columnDefinition = "TEXT")
    private String reponseCorrecte;

    @Column(name = "temps_alloue_sec")
    private Integer tempsAlloueSec;

    @Column(name = "numero_ordre")
    private Integer numeroOrdre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EvaluationDiagnostique diagnostic;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ReponseDiagnostique reponse;
}
