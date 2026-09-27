package com.app.vendia.controller;

import com.app.vendia.dto.request.ProduitRequest;
import com.app.vendia.dto.response.ProduitResponse;
import com.app.vendia.service.ProduitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/produits")
@RequiredArgsConstructor
@Tag(name = "Produits")
public class ProduitController {

    private final ProduitService produitService;

    
    @GetMapping
    public Page<ProduitResponse> lister(
            @RequestParam(required = false) Long categorieId,
            Pageable pageable
    ) {
        return produitService.lister(categorieId, pageable);
    }

    
    @GetMapping("/recherche")
    public Page<ProduitResponse> rechercher(@RequestParam String q, Pageable pageable) {
        return produitService.rechercher(q, pageable);
    }

    @GetMapping("/{id}")
    public ProduitResponse detail(@PathVariable Long id) {
        return produitService.detail(id);
    }

    @PostMapping("/admin")
    public ResponseEntity<ProduitResponse> creer(@Valid @RequestBody ProduitRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produitService.creer(requete));
    }

    @PutMapping("/admin/{id}")
    public ProduitResponse modifier(@PathVariable Long id, @Valid @RequestBody ProduitRequest requete) {
        return produitService.modifier(id, requete);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        produitService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
