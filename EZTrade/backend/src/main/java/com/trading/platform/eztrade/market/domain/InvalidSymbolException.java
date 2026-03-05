package com.trading.platform.eztrade.market.domain;

/**
 * Excepción de dominio que indica que se ha intentado crear o utilizar
 * un símbolo (ticker) que no cumple las reglas de validación establecidas.
 */
public class InvalidSymbolException extends RuntimeException {
    public InvalidSymbolException(String message) {
        super(message);
    }
}
