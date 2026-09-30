package com.app.vendia.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;


public record CommandeRequest(
        Long adresseId,
        @Valid AdresseRequest adresse
) {
    @JsonIgnore
    @AssertTrue(message = "Fournir adresseId ou adresse")
    public boolean isAdresseFournie() {
        return adresseId != null || adresse != null;
    }
}
