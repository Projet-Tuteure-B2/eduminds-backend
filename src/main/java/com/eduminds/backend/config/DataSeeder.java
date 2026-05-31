package com.eduminds.backend.config;

import com.eduminds.backend.entity.Filiere;
import com.eduminds.backend.entity.Matiere;
import com.eduminds.backend.entity.User;
import com.eduminds.backend.repository.FiliereRepository;
import com.eduminds.backend.repository.MatiereRepository;
import com.eduminds.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final FiliereRepository filiereRepository;
    private final MatiereRepository matiereRepository;
    private final UserRepository userRepository;

    // Matières prédéfinies par filière
    // Format : code_filiere -> liste de noms de matières
    private static final Map<String, List<String>> MATIERES_PAR_FILIERE = Map.of(
        "DROIT", List.of(
            "Droit civil", "Droit pénal", "Droit constitutionnel",
            "Droit commercial", "Droit administratif", "Procédure civile",
            "Droit international privé", "Droit des affaires"
        ),
        "SCI", List.of(
            "Biochimie", "Physiologie", "Biologie cellulaire",
            "Chimie organique", "Physique", "Mathématiques", "Statistiques"
        ),
        "MED", List.of(
            "Anatomie", "Physiologie médicale", "Sémiologie",
            "Pharmacologie", "Biochimie médicale", "Microbiologie",
            "Pathologie générale", "Immunologie"
        ),
        "INFO", List.of(
            "Algorithmique", "Programmation orientée objet", "Base de données",
            "Réseaux informatiques", "Systèmes d'exploitation",
            "Développement web", "Sécurité informatique", "Intelligence artificielle"
        ),
        "ECO", List.of(
            "Microéconomie", "Macroéconomie", "Comptabilité générale",
            "Finance d'entreprise", "Marketing", "Management",
            "Statistiques économiques", "Fiscalité"
        ),
        "LETTRES", List.of(
            "Littérature française", "Littérature africaine", "Linguistique",
            "Grammaire avancée", "Histoire de la littérature",
            "Anglais littéraire", "Philosophie", "Stylistique"
        ),
        "PHILO", List.of(
            "Philosophie générale", "Éthique", "Logique",
            "Histoire de la philosophie", "Épistémologie",
            "Philosophie politique", "Métaphysique"
        ),
        "GESTION", List.of(
            "Comptabilité analytique", "Gestion des ressources humaines",
            "Contrôle de gestion", "Audit", "Finance d'entreprise",
            "Management stratégique", "Entrepreneuriat", "Fiscalité des entreprises"
        )
    );

    private static final Map<String, String> NOMS_FILIERES = Map.of(
        "DROIT",   "Droit",
        "SCI",     "Sciences",
        "MED",     "Médecine",
        "INFO",    "Informatique",
        "ECO",     "Économie",
        "LETTRES", "Lettres",
        "PHILO",   "Philosophie",
        "GESTION", "Gestion"
    );

    @Override
    @Transactional
    public void run(String... args) {
        seedFilieres();
    }

    private void seedFilieres() {
        for (Map.Entry<String, String> entry : NOMS_FILIERES.entrySet()) {
            String code = entry.getKey();
            String nom  = entry.getValue();

            if (!filiereRepository.existsByCode(code)) {
                Filiere filiere = Filiere.builder()
                        .code(code)
                        .nom(nom)
                        .build();
                filiereRepository.save(filiere);
                log.info("✅ Filière créée : {}", nom);
            }
        }
        log.info("🌱 Seed filières terminé ({} filières)", filiereRepository.count());
    }

    /**
     * Appelé par AuthService après inscription d'un étudiant.
     * Crée les matières prédéfinies pour sa filière et les associe à son compte.
     */
    @Transactional
    public void seedMatieresForUser(User user, String filiereCode) {
        Filiere filiere = filiereRepository.findByCode(filiereCode.toUpperCase()).orElse(null);
        List<String> noms = MATIERES_PAR_FILIERE.getOrDefault(filiereCode.toUpperCase(), List.of());

        for (String nom : noms) {
            // Vérifier si la matière n'existe pas déjà pour cet utilisateur
            boolean exists = matiereRepository.findByUserId(user.getId())
                    .stream()
                    .anyMatch(m -> m.getNom().equalsIgnoreCase(nom));

            if (!exists) {
                Matiere matiere = Matiere.builder()
                        .nom(nom)
                        .estCustom(false)
                        .filiere(filiere)
                        .user(user)
                        .build();
                matiereRepository.save(matiere);
            }
        }
        log.info("🌱 {} matières créées pour {} (filière {})", noms.size(), user.getEmail(), filiereCode);
    }

    public List<String> getMatieresPourFiliere(String filiereCode) {
        return MATIERES_PAR_FILIERE.getOrDefault(filiereCode.toUpperCase(), List.of());
    }
}
