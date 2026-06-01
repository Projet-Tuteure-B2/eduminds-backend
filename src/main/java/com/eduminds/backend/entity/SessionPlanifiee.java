package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "sessions_planifiees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionPlanifiee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_prevue", nullable = false)
    private LocalDate datePrevue;

    @Column(name = "nb_cartes_prevues")
    private Integer nbCartesPrevues;

    // PLANIFIEE / EFFECTUEE / MANQUEE
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutSession statut = StatutSession.PLANIFIEE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Planning planning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapitre_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Chapitre chapitre;
}
