package com.app.vendia.exception;

public class AjustementStockInvalideException extends RuntimeException {
    public AjustementStockInvalideException(String produitNom, int stockActuel, int delta) {
        super("Ajustement refusé pour \"" + produitNom + "\" : stock actuel " + stockActuel
                + ", delta " + delta + " donnerait un stock négatif");
    }
}
