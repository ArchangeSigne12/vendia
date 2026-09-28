package com.app.vendia.dto.response;

import java.time.LocalDateTime;

public record ErreurReponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {
    public static ErreurReponse of(int status, String error, String message) {
        return new ErreurReponse(LocalDateTime.now(), status, error, message);
    }
}
