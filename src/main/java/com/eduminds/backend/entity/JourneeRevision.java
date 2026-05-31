package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "journees_revision",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JourneeRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "nb_cartes_revisees")
    @Builder.Default
    private Integer nbCartesRevisees = 0;

    // Intensité calculée pour la heatmap : 0 = rien, 1-4 = niveaux croissants
    @Builder.Default
    private Integer intensite = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
}
