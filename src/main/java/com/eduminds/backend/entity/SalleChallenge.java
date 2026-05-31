package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "salles_challenge")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalleChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Code unique format SYN-XXXX, DÉTRUIT (ligne supprimée) à la fermeture
    @Column(name = "code_acces", unique = true)
    private String codeAcces;

    // QR Code en base64 PNG
    @Column(name = "qr_code_base64", columnDefinition = "TEXT")
    private String qrCodeBase64;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutChallenge statut = StatutChallenge.EN_ATTENTE;

    @Column(name = "nb_questions")
    private Integer nbQuestions;    // Entre 20 et 35

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_fermeture")
    private LocalDateTime dateFermeture;

    // Matière sur laquelle porte le challenge
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matiere_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Matiere matiere;

    // Hôte de la salle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hote_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User hote;

    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ParticipationChallenge> participations;

    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<QuestionChallenge> questions;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}
