package com.trading.platform.eztrade.trading.domain;

import java.math.BigDecimal;

/**
 * Value object representing the traded quantity of an asset.
 * <p>
 * Guarantees that the quantity is strictly positive.
 *
 * @param value quantity in asset units
 */
public record Quantity(BigDecimal value) {

    /**
     * Compact constructor with domain validation.
     *
     * @throws TradingDomainException if the quantity is null or less than/equal to zero
     */
    public Quantity {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TradingDomainException("La cantidad debe ser mayor que cero");
        }
    }
}
