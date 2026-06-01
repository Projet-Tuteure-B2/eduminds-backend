package com.eduminds.backend.entity;

public enum StatutChallenge {
    EN_ATTENTE,   // Salle créée, en attente de participants
    EN_COURS,     // Challenge démarré
    TERMINEE,     // Challenge terminé normalement
    ANNULEE       // Annulé par l'hôte ou faute de participants
}
