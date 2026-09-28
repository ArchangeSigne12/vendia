package com.app.vendia.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PanierResponse(
        Long id,
        List<LignePanierResponse> lignes,
        BigDecimal total
) {}
