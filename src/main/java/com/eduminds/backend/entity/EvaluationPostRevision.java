package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "evaluations_post_revision")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationPostRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    // Score final sur 100
    @Column(name = "score_final")
    private Float scoreFinal;

    // true = terminée normalement, false = abandonnée (anti-triche → annulée)
    @Builder.Default
    private Boolean terminee = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_revision_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SessionRevision sessionRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<QuestionEvaluation> questions;
}
