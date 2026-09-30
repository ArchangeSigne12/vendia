package com.app.vendia.dto.response;

public record AdresseResponse(
        Long id,
        String intitule,
        String ligne1,
        String ligne2,
        String codePostal,
        String ville,
        String pays
) {}
