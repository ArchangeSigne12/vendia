package com.app.vendia.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AjoutLigneRequest(

        @NotNull(message = "Le produit est obligatoire")
        Long produitId,

        @NotNull @Min(value = 1, message = "La quantité doit être d'au moins 1")
        Integer quantite
) {}
