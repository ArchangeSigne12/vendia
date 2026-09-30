package com.app.vendia.exception;

import com.app.vendia.model.StatutCommande;

public class CommandeNonAnnulableException extends RuntimeException {
    public CommandeNonAnnulableException(StatutCommande statut) {
        super("Une commande au statut " + statut + " ne peut plus être annulée");
    }
}
