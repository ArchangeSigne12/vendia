package com.app.vendia.dto.response;

import com.app.vendia.model.StatutCommande;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminCommandeResponse(
        Long id,
        Long clientId,
        String clientNom,
        String clientEmail,
        LocalDateTime dateCreation,
        StatutCommande statut,
        int nombreArticles,
        BigDecimal total
) {}
