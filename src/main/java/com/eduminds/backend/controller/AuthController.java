package com.eduminds.backend.controller;

import com.eduminds.backend.dto.AuthRequest;
import com.eduminds.backend.dto.AuthResponse;
import com.eduminds.backend.dto.RegisterRequest;
import com.eduminds.backend.entity.User;
import com.eduminds.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Vérifier si l'email existe déjà
            if (userRepository.existsByEmail(request.getEmail())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cet email est déjà utilisé");
                return ResponseEntity.badRequest().body(error);
            }

            // Créer l'utilisateur
            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword()); // À hasher plus tard
            user.setFiliere(request.getFiliere());
            user.setLevel(request.getLevel());
            user.setXpTotal(0);
            user.setStreak(0);

            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilisateur créé avec succès");
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de l'inscription: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElse(null);

            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email ou mot de passe incorrect");
                return ResponseEntity.badRequest().body(error);
            }

            if (!user.getPassword().equals(request.getPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email ou mot de passe incorrect");
                return ResponseEntity.badRequest().body(error);
            }

            // Pour l'instant, on retourne les infos sans JWT
            Map<String, String> response = new HashMap<>();
            response.put("token", "fake-jwt-token-" + System.currentTimeMillis());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("filiere", user.getFiliere());
            response.put("level", user.getLevel());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la connexion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}