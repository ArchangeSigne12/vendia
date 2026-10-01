package com.app.vendia.dto.response;

import java.math.BigDecimal;

public record TableauDeBordResponse(
        long nombreCommandes,
        long nombreCommandesEnAttente,
        long nombreCommandesEnPreparation,
        BigDecimal chiffreAffaires, 
        long produitsEnRupture
) {}
