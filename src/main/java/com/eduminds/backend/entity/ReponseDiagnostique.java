package com.eduminds.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reponses_diagnostiques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReponseDiagnostique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reponse_donnee", columnDefinition = "TEXT")
    private String reponseDonnee;

    @Column(name = "est_correcte")
    private Boolean estCorrecte;

    @Column(name = "temps_reponse_sec")
    private Integer tempsReponseSec;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private QuestionDiagnostique question;
}
