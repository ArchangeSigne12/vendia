package com.app.vendia.dto.request;

import com.app.vendia.model.StatutCommande;
import jakarta.validation.constraints.NotNull;

public record ChangerStatutRequest(
        @NotNull(message = "Le statut cible est obligatoire")
        StatutCommande statut
) {}
