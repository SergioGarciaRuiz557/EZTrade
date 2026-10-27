package com.trading.platform.eztrade.trading.domain;

import java.math.BigDecimal;

/**
 * Value object for representing monetary amounts in the domain.
 * <p>
 * Used both for unit price and total amount.
 *
 * @param value monetary amount
 */
public record Money(BigDecimal value) {

    /**
     * Compact constructor with domain validation.
     *
     * @throws TradingDomainException if the amount is null or less than/equal to zero
     */
    public Money {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TradingDomainException("El valor monetario debe ser mayor que cero");
        }
    }

    /**
     * Multiplies the amount by a quantity to obtain a total.
     *
     * @param quantity asset quantity
     * @return resulting amount from the multiplication
     */
    public Money multiply(Quantity quantity) {
        return new Money(value.multiply(quantity.value()));
    }
}
