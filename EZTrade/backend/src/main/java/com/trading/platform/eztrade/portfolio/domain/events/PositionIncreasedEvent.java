package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a BUY increases an existing position.
 *
 * @param owner user that owns the position
 * @param symbol normalized symbol
 * @param quantity total quantity after the increase
 * @param averageCost new weighted average cost
 * @param occurredAt event publication timestamp
 */
public record PositionIncreasedEvent(
        String owner,
        String symbol,
        BigDecimal quantity,
        BigDecimal averageCost,
        LocalDateTime occurredAt
) {
}

