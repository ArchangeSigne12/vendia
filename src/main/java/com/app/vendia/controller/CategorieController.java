package com.app.vendia.controller;

import com.app.vendia.dto.request.CategorieRequest;
import com.app.vendia.dto.response.CategorieResponse;
import com.app.vendia.service.CategorieService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Catégories")
public class CategorieController {

    private final CategorieService categorieService;

    @GetMapping
    public List<CategorieResponse> lister() {
        return categorieService.lister();
    }

    @PostMapping("/admin")
    public ResponseEntity<CategorieResponse> creer(@Valid @RequestBody CategorieRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categorieService.creer(requete));
    }

    @PutMapping("/admin/{id}")
    public CategorieResponse modifier(@PathVariable Long id, @Valid @RequestBody CategorieRequest requete) {
        return categorieService.modifier(id, requete);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        categorieService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
