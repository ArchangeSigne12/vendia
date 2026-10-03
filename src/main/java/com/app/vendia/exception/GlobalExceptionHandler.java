package com.app.vendia.exception;

import com.app.vendia.dto.response.ErreurReponse;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailDejaUtiliseException.class)
    public ResponseEntity<ErreurReponse> gererEmailDejaUtilise(EmailDejaUtiliseException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErreurReponse.of(409, "EMAIL_DEJA_UTILISE", e.getMessage()));
    }

    @ExceptionHandler({IdentifiantsInvalidesException.class, BadCredentialsException.class})
    public ResponseEntity<ErreurReponse> gererIdentifiantsInvalides(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErreurReponse.of(401, "IDENTIFIANTS_INVALIDES", "Email ou mot de passe incorrect"));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErreurReponse> gererJwtInvalide(JwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErreurReponse.of(401, "TOKEN_INVALIDE", "Token invalide ou expiré"));
    }

    @ExceptionHandler({StockInsuffisantException.class, TransitionStatutInvalideException.class})
    public ResponseEntity<ErreurReponse> gererConflit(RuntimeException e) {
        String code = (e instanceof StockInsuffisantException)
                ? "STOCK_INSUFFISANT"
                : "TRANSITION_STATUT_INVALIDE";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErreurReponse.of(409, code, e.getMessage()));
    }

    @ExceptionHandler(PanierVideException.class)
    public ResponseEntity<ErreurReponse> gererPanierVide(PanierVideException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErreurReponse.of(400, "PANIER_VIDE", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurReponse> gererValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " : " + fe.getDefaultMessage())
                .orElse("Requête invalide");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErreurReponse.of(400, "VALIDATION_ERREUR", message));
    }
}