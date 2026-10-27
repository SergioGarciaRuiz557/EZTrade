package com.trading.platform.eztrade.trading.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event emitted to request order execution.
 *
 * @param orderId order identifier
 * @param owner order owner
 * @param symbol asset symbol
 * @param side order side (BUY/SELL)
 * @param quantity requested quantity
 * @param price execution unit price
 * @param occurredAt event emission date and time
 */
public record OrderExecutionRequestEvent(
        Long orderId,
        String owner,
        String symbol,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        LocalDateTime occurredAt
) {
}
