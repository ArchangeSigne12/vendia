package com.app.vendia.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProduitRequest(

        @NotNull(message = "La catégorie est obligatoire")
        Long categorieId,

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotNull @DecimalMin(value = "0.0", message = "Le prix doit être positif")
        BigDecimal prix,

        String description,

        @NotNull @Min(value = 0, message = "Le stock ne peut pas être négatif")
        Integer stock,

        String image
) {}
