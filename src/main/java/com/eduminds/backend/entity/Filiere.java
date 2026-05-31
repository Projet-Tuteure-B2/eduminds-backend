package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "filieres")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Filiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;          // ex. "Droit", "Sciences", "Médecine"

    @Column(nullable = false, unique = true)
    private String code;         // ex. "DROIT", "SCI", "MED"

    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Matiere> matieres;
}
