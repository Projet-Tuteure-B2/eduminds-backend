package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "flashcards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reponseReference;   // Réponse correcte de référence

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeQuestion type;         // QCM / QRO / VRAI_FAUX

    // Pour les QCM : options séparées par "||"  ex. "Paris||Lyon||Marseille||Bordeaux"
    @Column(columnDefinition = "TEXT")
    private String optionsQcm;

    // Indice de difficulté estimé par Claude (1=facile, 3=difficile)
    @Builder.Default
    private Integer difficulte = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapitre_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Chapitre chapitre;

    // Progression Leitner de cette carte pour l'étudiant
    @OneToOne(mappedBy = "flashcard", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProgressionCarte progression;
}
