package com.eduminds.backend.exception;

import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Erreurs de validation (@Valid) ────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valeur invalide",
                        (a, b) -> a));

        return ResponseEntity.badRequest().body(errorBody(
                HttpStatus.BAD_REQUEST,
                "Données invalides",
                fieldErrors
        ));
    }

    // ── Mauvais credentials (email/mot de passe) ──────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(
                HttpStatus.UNAUTHORIZED,
                "Email ou mot de passe incorrect",
                null
        ));
    }

    // ── IllegalArgumentException (email déjà utilisé, etc.) ──────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(errorBody(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null
        ));
    }

    // ── Accès refusé (endpoint protégé sans token) ────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(
                HttpStatus.FORBIDDEN,
                "Accès refusé",
                null
        ));
    }

    // ── Toute autre exception non gérée ──────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur interne du serveur",
                null
        ));
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private Map<String, Object> errorBody(HttpStatus status, String message, Object details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     message);
        if (details != null) body.put("details", details);
        return body;
    }
}
