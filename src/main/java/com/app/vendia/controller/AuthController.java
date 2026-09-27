package com.app.vendia.controller;

import com.app.vendia.dto.response.*;
import com.app.vendia.dto.request.*;
import com.app.vendia.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")

    public ResponseEntity<AuthResponse> inscrire(@Valid @RequestBody RegisterRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrire(requete));
    }

    @PostMapping("/login")

    public ResponseEntity<AuthResponse> connecter(@Valid @RequestBody LoginRequest requete) {
        return ResponseEntity.ok(authService.connecter(requete));
    }

    @PostMapping("/refresh")
    
    public ResponseEntity<AuthResponse> rafraichir(@Valid @RequestBody RefreshRequest requete) {
        return ResponseEntity.ok(authService.rafraichir(requete));
    }
}
