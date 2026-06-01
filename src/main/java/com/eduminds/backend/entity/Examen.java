package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "examens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;                // ex. "Examen de Droit civil S2"

    @Column(name = "date_examen", nullable = false)
    private LocalDate dateExamen;        // Détermine la durée du planning

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ImportanceExamen importance = ImportanceExamen.NORMALE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
}
