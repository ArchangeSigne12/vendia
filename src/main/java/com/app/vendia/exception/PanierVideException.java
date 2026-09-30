package com.app.vendia.exception;

public class PanierVideException extends RuntimeException {
    public PanierVideException() {
        super("Impossible de commander : le panier est vide");
    }
}
