package com.app.vendia.exception;

import com.app.vendia.model.StatutCommande;

public class TransitionStatutInvalideException extends RuntimeException {
    public TransitionStatutInvalideException(StatutCommande actuel, StatutCommande cible) {
        super("Impossible de passer une commande de " + actuel + " à " + cible);
    }
}
