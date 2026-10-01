package com.app.vendia.controller;

import com.app.vendia.dto.request.ChangerStatutRequest;
import com.app.vendia.dto.response.AdminCommandeResponse;
import com.app.vendia.model.StatutCommande;
import com.app.vendia.service.AdminCommandeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/admin/commandes")
@RequiredArgsConstructor
@Tag(name = "Admin - Commandes")
public class AdminCommandeController {

    private final AdminCommandeService adminCommandeService;

    @GetMapping
    public Page<AdminCommandeResponse> lister(
            @RequestParam(required = false) StatutCommande statut,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return adminCommandeService.lister(statut, pageable);
    }

    @GetMapping("/{id}")
    public AdminCommandeResponse detail(@PathVariable Long id) {
        return adminCommandeService.detail(id);
    }

    @PutMapping("/{id}/statut")
    public AdminCommandeResponse changerStatut(
            @PathVariable Long id,
            @Valid @RequestBody ChangerStatutRequest requete
    ) {
        return adminCommandeService.changerStatut(id, requete.statut());
    }
}
