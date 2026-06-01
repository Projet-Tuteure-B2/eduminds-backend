package com.eduminds.backend.entity;

public enum TypeNotification {
    RAPPEL_REVISION,        // Push système : cartes en attente
    STREAK_EN_DANGER,       // Push système : streak va sauter
    EXAMEN_PROCHE,          // Push système : examen dans X jours
    FELICITATIONS,          // Push système : après une bonne session
    CHALLENGE_INVITATION,   // In-app : invitation à rejoindre une salle
    CHALLENGE_DEMARRE,      // In-app : le challenge a démarré
    CHALLENGE_RESULTATS,    // In-app : résultats disponibles
    COURS_PRET              // In-app : flashcards générées et prêtes
}
