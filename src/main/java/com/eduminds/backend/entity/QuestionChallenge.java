package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "questions_challenge")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionChallenge {

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

    // Temps alloué visible dans le chronomètre (12-20 sec selon complexité)
    @Column(name = "temps_alloue_sec")
    @Builder.Default
    private Integer tempsAlloueSec = 15;

    // Points max attribuables pour cette question (score Kahoot dégressif)
    @Column(name = "points_max")
    @Builder.Default
    private Integer pointsMax = 1000;

    @Column(name = "numero_ordre")
    private Integer numeroOrdre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SalleChallenge salle;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ReponseChallenge> reponses;
}
