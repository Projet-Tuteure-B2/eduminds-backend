package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "evaluations_diagnostiques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationDiagnostique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Contexte du déclenchement
    @Enumerated(EnumType.STRING)
    @Column(name = "contexte")
    private ContexteDiagnostic contexte;  // INSCRIPTION ou CHANGEMENT_FILIERE

    @Column(name = "filiere_evaluee")
    private String filiereEvaluee;

    @Column(name = "niveau_evalue")
    private String niveauEvalue;

    // Niveau calibré par Claude après le diagnostic
    @Column(name = "niveau_calibre_resultat")
    private String niveauCalibreResultat;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    // false = abandonnée (anti-triche), inscription non validée
    @Builder.Default
    private Boolean terminee = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "diagnostic", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<QuestionDiagnostique> questions;
}
