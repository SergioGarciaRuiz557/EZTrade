package com.trading.platform.eztrade.trading.domain.events;

import java.time.LocalDateTime;

/**
 * Domain event emitted when an order is cancelled.
 *
 * @param orderId cancelled order identifier
 * @param owner order owner
 * @param symbol asset symbol
 * @param occurredAt event emission date and time
 */
public record OrderCancelledEvent(
        Long orderId,
        String owner,
        String symbol,
        LocalDateTime occurredAt
) {
}
