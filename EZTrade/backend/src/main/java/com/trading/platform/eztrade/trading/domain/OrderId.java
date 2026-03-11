package com.trading.platform.eztrade.trading.domain;

/**
 * Value object que representa el identificador de una orden.
 * <p>
 * Encapsula la regla de dominio de que un id valido debe ser positivo.
 * Se utiliza para evitar el uso de valores primitivos sin semantica.
 *
 * @param value valor numerico del identificador
 */
public record OrderId(Long value) {

    /**
     * Constructor compacto con validacion de invariantes.
     *
     * @throws TradingDomainException si el id es nulo o no positivo
     */
    public OrderId {
        if (value == null || value <= 0) {
            throw new TradingDomainException("Order id must be positive");
        }
    }
}
