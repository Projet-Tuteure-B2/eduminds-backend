package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sessions_revision")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "nb_cartes_traitees")
    @Builder.Default
    private Integer nbCartesTraitees = 0;

    // true = session terminée normalement, false = abandonnée (anti-triche)
    @Builder.Default
    private Boolean terminee = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapitre_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Chapitre chapitre;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<EvaluationCarte> evaluations;
}
