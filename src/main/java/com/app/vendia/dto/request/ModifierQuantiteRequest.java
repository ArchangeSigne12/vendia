package com.app.vendia.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ModifierQuantiteRequest(

        @NotNull @Min(value = 1, message = "La quantité doit être d'au moins 1 (utilisez DELETE pour retirer la ligne)")
        Integer quantite
) {}
