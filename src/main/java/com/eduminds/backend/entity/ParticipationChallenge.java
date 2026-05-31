package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "participations_challenge")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ACTIF / FORFAIT (quitte pendant le challenge) / TERMINE
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutParticipation statut = StatutParticipation.ACTIF;

    @Builder.Default
    private Integer score = 0;

    @Column(name = "rang_final")
    private Integer rangFinal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SalleChallenge salle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
}
