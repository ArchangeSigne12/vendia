package com.app.vendia.exception;

public class EmailDejaUtiliseException extends RuntimeException {
    public EmailDejaUtiliseException(String email) {
        super("Un compte existe déjà avec l'email : " + email);
    }
}
