package com.app.vendia.dto.response;

import java.math.BigDecimal;

public record ProduitResponse(
        Long id,
        Long categorieId,
        String categorieNom,
        String nom,
        BigDecimal prix,
        String description,
        Integer stock,
        boolean actif,
        String image,
        boolean disponible 
) {}
