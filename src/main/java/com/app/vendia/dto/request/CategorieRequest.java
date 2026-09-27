package com.app.vendia.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategorieRequest(
        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        String nom,
        String description
) {}
