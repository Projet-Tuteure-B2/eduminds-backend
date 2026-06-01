package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "matieres")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Matiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "est_custom")
    @Builder.Default
    private Boolean estCustom = false;   // true = créée par l'étudiant

    private String couleur;              // ex. "#4CAF50" pour custom

    // Lien vers la filière prédéfinie (null si custom orpheline)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Filiere filiere;

    // Propriétaire (toujours renseigné)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "matiere", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Chapitre> chapitres;
}
