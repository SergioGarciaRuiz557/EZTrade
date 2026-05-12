package com.trading.platform.eztrade.portfolio.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Valoracion de mercado de una posicion abierta.
 */
public record PositionMarketValuation(
        String symbol,
        BigDecimal currentPrice,
        BigDecimal marketValue,
        BigDecimal unrealizedPnl
) {

    public static PositionMarketValuation from(Position position, BigDecimal currentPrice) {
        Objects.requireNonNull(position, "Position is required");
        Objects.requireNonNull(currentPrice, "Current price is required");

        BigDecimal marketValue = currentPrice.multiply(position.quantity());
        BigDecimal unrealizedPnl = currentPrice.subtract(position.averageCost()).multiply(position.quantity());

        return new PositionMarketValuation(
                position.symbol(),
                currentPrice,
                marketValue,
                unrealizedPnl
        );
    }
}
