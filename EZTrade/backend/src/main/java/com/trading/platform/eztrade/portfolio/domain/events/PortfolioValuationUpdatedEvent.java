package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary event published when portfolio recalculates a portfolio aggregate
 * picture.
 * <p>
 * Includes available cash projected from wallet, the cost basis of open
 * positions, and accumulated realized PnL.
 */
public record PortfolioValuationUpdatedEvent(
        String owner,
        BigDecimal cashAvailable,
        BigDecimal totalCostBasis,
        BigDecimal totalRealizedPnl,
        LocalDateTime occurredAt
) {
}

