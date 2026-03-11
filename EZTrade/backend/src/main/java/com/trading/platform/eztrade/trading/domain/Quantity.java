package com.trading.platform.eztrade.trading.domain;

import java.math.BigDecimal;

/**
 * Value object que representa la cantidad negociada de un activo.
 * <p>
 * Garantiza que la cantidad sea estrictamente positiva.
 *
 * @param value cantidad en unidades del activo
 */
public record Quantity(BigDecimal value) {

    /**
     * Constructor compacto con validacion de dominio.
     *
     * @throws TradingDomainException si la cantidad es nula o menor/igual a cero
     */
    public Quantity {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TradingDomainException("Quantity must be greater than zero");
        }
    }
}
