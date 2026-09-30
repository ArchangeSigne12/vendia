package com.app.vendia.dto.response;

import java.math.BigDecimal;

public record LigneCommandeResponse(
        Long produitId,
        String produitNom,
        String image,
        BigDecimal prixUnitaire, 
        Integer quantite,
        BigDecimal sousTotal
) {}
