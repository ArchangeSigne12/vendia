package com.app.vendia.exception;

public class RessourceIntrouvableException extends RuntimeException {
    public RessourceIntrouvableException(String ressource, Long id) {
        super(ressource + " introuvable avec l'id : " + id);
    }
}
