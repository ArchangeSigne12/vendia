package com.app.vendia.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdresseRequest(
        String intitule,

        @NotBlank(message = "La ligne d'adresse est obligatoire")
        String ligne1,

        String ligne2,

        @NotBlank(message = "Le code postal est obligatoire")
        String codePostal,

        @NotBlank(message = "La ville est obligatoire")
        String ville,

        @NotBlank(message = "Le pays est obligatoire")
        String pays
) {}
