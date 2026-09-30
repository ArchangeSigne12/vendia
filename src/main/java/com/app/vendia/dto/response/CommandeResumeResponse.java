package com.app.vendia.dto.response;

import com.app.vendia.model.StatutCommande;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CommandeResumeResponse(
        Long id,
        LocalDateTime dateCreation,
        StatutCommande statut,
        int nombreArticles,
        BigDecimal total
) {}
