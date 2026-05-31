package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fichiers_source")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichierSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeSource type;         // PHOTO / PDF / TEXTE

    @Column(name = "chemin_fichier")
    private String cheminFichier;    // chemin sur le serveur (null si TEXTE)

    @Column(columnDefinition = "TEXT")
    private String contenuTexte;     // contenu extrait (après OCR ou extraction PDF)

    @Column(name = "nom_original")
    private String nomOriginal;      // nom du fichier uploadé

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapitre_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Chapitre chapitre;
}
