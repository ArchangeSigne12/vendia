package com.app.vendia.controller;

import com.app.vendia.dto.request.CommandeRequest;
import com.app.vendia.dto.response.CommandeResponse;
import com.app.vendia.dto.response.CommandeResumeResponse;
import com.app.vendia.security.VendiaUserDetails;
import com.app.vendia.service.CommandeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Routes couvertes par anyRequest().authenticated() : aucune modification
 * de SecurityConfig n'est nécessaire. Le client est toujours celui du token.
 */
@RestController
@RequestMapping("/api/v1/commandes")
@RequiredArgsConstructor
@Tag(name = "Commandes")
public class CommandeController {

    private final CommandeService commandeService;

    @PostMapping
    public ResponseEntity<CommandeResponse> creer(
            @AuthenticationPrincipal VendiaUserDetails principal,
            @Valid @RequestBody CommandeRequest requete
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandeService.creer(principal.getClientId(), requete));
    }

    // Historique : plus récentes d'abord, paginé (ex. ?page=0&size=10)
    @GetMapping
    public Page<CommandeResumeResponse> historique(
            @AuthenticationPrincipal VendiaUserDetails principal,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return commandeService.historique(principal.getClientId(), pageable);
    }

    @GetMapping("/{id}")
    public CommandeResponse detail(
            @AuthenticationPrincipal VendiaUserDetails principal,
            @PathVariable Long id
    ) {
        return commandeService.detail(principal.getClientId(), id);
    }

    @PostMapping("/{id}/annuler")
    public CommandeResponse annuler(
            @AuthenticationPrincipal VendiaUserDetails principal,
            @PathVariable Long id
    ) {
        return commandeService.annuler(principal.getClientId(), id);
    }
}
