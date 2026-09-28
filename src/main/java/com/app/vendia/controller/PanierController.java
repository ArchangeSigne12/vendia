package com.app.vendia.controller;

import com.app.vendia.dto.request.AjoutLigneRequest;
import com.app.vendia.dto.request.ModifierQuantiteRequest;
import com.app.vendia.dto.response.PanierResponse;
import com.app.vendia.security.VendiaUserDetails;
import com.app.vendia.service.PanierService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Toutes les routes exigent un client authentifié (voir SecurityConfig :
 * couvertes par anyRequest().authenticated(), aucune n'est dans permitAll).
 * Le panier manipulé est toujours celui du client du token — jamais un id
 * de panier passé en paramètre, pour empêcher un client d'agir sur le
 * panier d'un autre.
 */
@RestController
@RequestMapping("/api/v1/panier")
@RequiredArgsConstructor
@Tag(name = "Panier")
public class PanierController {

    private final PanierService panierService;

    @GetMapping
    public PanierResponse voir(@AuthenticationPrincipal VendiaUserDetails principal) {
        return panierService.voirPanier(principal.getClientId());
    }

    @PostMapping("/lignes")
    public PanierResponse ajouter(
            @AuthenticationPrincipal VendiaUserDetails principal,
            @Valid @RequestBody AjoutLigneRequest requete
    ) {
        return panierService.ajouterLigne(principal.getClientId(), requete.produitId(), requete.quantite());
    }

    @PutMapping("/lignes/{produitId}")
    public PanierResponse modifier(
            @AuthenticationPrincipal VendiaUserDetails principal,
            @PathVariable Long produitId,
            @Valid @RequestBody ModifierQuantiteRequest requete
    ) {
        return panierService.modifierQuantite(principal.getClientId(), produitId, requete.quantite());
    }

    @DeleteMapping("/lignes/{produitId}")
    public PanierResponse supprimerLigne(
            @AuthenticationPrincipal VendiaUserDetails principal,
            @PathVariable Long produitId
    ) {
        return panierService.supprimerLigne(principal.getClientId(), produitId);
    }

    @DeleteMapping
    public ResponseEntity<Void> vider(@AuthenticationPrincipal VendiaUserDetails principal) {
        panierService.viderPanier(principal.getClientId());
        return ResponseEntity.noContent().build();
    }
}
