package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "progressions_carte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressionCarte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Boîte Leitner actuelle : 1 à 6 (6 = Maîtrisée)
    @Builder.Default
    private Integer boite = 1;

    // Prochaine date de révision calculée par LeitnerService
    @Column(name = "prochaine_revision")
    private LocalDate prochaineRevision;

    // Nb de réussites consécutives (pour passer en boîte 6 = Maîtrisée)
    @Column(name = "reussites_consecutives")
    @Builder.Default
    private Integer reussitesConsecutives = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Flashcard flashcard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
}
