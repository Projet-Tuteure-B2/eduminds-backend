package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chapitres")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapitre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutTraitement statut = StatutTraitement.EN_TRAITEMENT;

    @Column(name = "message_erreur", columnDefinition = "TEXT")
    private String messageErreur;    // renseigné si statut = ERREUR

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Matiere matiere;

    // Fichiers sources qui composent ce chapitre (1 à 5)
    @OneToMany(mappedBy = "chapitre", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<FichierSource> sources;

    // Flashcards générées par Claude pour ce chapitre
    @OneToMany(mappedBy = "chapitre", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Flashcard> flashcards;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
