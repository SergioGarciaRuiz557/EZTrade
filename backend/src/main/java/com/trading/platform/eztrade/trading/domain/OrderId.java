package com.trading.platform.eztrade.trading.domain;

/**
 * Value object representing an order identifier.
 * <p>
 * Encapsulates the domain rule that a valid id must be positive. Used to avoid
 * raw primitive values without semantics.
 *
 * @param value numeric identifier value
 */
public record OrderId(Long value) {

    /**
     * Compact constructor with invariant validation.
     *
     * @throws TradingDomainException if the id is null or not positive
     */
    public OrderId {
        if (value == null || value <= 0) {
            throw new TradingDomainException("El id de la orden debe ser positivo");
        }
    }
}
