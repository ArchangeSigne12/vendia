package com.app.vendia.dto.response;

import com.app.vendia.model.StatutCommande;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CommandeResponse(
        Long id,
        LocalDateTime dateCreation,
        StatutCommande statut,
        AdresseResponse adresse,
        List<LigneCommandeResponse> lignes,
        BigDecimal total
) {}
