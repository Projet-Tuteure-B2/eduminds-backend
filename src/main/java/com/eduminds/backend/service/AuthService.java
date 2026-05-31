package com.eduminds.backend.service;

import com.eduminds.backend.dto.*;
import com.eduminds.backend.entity.User;
import com.eduminds.backend.repository.UserRepository;
import com.eduminds.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Inscription d'un nouvel étudiant.
     * Le compte est créé avec diagnosticPassed = false.
     * L'inscription n'est définitivement validée qu'après le diagnostic (Phase 7).
     */
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .filiere(request.getFiliere())
                .level(request.getLevel())
                .xpTotal(0)
                .streak(0)
                .diagnosticPassed(false)   // Validé seulement après diagnostic
                .tokenVersion(0)
                .build();

        userRepository.save(user);

        // On génère un token même avant le diagnostic pour permettre
        // la navigation vers l'écran de diagnostic
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getTokenVersion());

        return buildResponse(token, user);
    }

    /**
     * Connexion.
     * Vérifie email + mot de passe BCrypt.
     */
    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }

        // Mise à jour de la date de dernière connexion
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getTokenVersion());

        return buildResponse(token, user);
    }

    /**
     * Déconnexion : incrémente tokenVersion pour invalider tous les tokens actifs.
     */
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
        });
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private AuthResponse buildResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .filiere(user.getFiliere())
                .level(user.getLevel())
                .niveauCalibre(user.getNiveauCalibre())
                .xpTotal(user.getXpTotal())
                .streak(user.getStreak())
                .diagnosticPassed(user.getDiagnosticPassed())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
