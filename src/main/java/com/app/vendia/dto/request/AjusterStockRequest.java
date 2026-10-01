package com.app.vendia.dto.request;

import jakarta.validation.constraints.NotNull;


public record AjusterStockRequest(
        @NotNull(message = "Le delta est obligatoire")
        Integer delta,

        String motif
) {}
