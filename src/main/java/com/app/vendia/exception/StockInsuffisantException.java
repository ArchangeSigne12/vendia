package com.app.vendia.exception;

public class StockInsuffisantException extends RuntimeException {
    public StockInsuffisantException(String produitNom, int stockDisponible, int quantiteDemandee) {
        super("Stock insuffisant pour \"" + produitNom + "\" : " + stockDisponible
                + " disponible(s), " + quantiteDemandee + " demandé(s)");
    }
}
