package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when portfolio creates a user's first position in a symbol
 * after executing a BUY.
 *
 * @param owner user that owns the position
 * @param symbol normalized symbol
 * @param quantity open quantity
 * @param averageCost initial average cost
 * @param occurredAt event publication timestamp
 */
public record PositionOpenedEvent(
        String owner,
        String symbol,
        BigDecimal quantity,
        BigDecimal averageCost,
        LocalDateTime occurredAt
) {
}

