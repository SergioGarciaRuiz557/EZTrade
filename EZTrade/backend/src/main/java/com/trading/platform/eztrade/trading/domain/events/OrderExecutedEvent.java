package com.trading.platform.eztrade.trading.domain.events;

import java.math.BigDecimal;

import java.time.LocalDateTime;

/**
 * Domain event emitted when an order is executed.
 *
 * @param orderId executed order identifier
 * @param owner order owner
 * @param symbol asset symbol
 * @param side executed order side (BUY/SELL)
 * @param quantity executed quantity
 * @param price execution unit price
 * @param occurredAt event emission date and time
 */
public record OrderExecutedEvent(
        Long orderId,
        String owner,
        String symbol,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        LocalDateTime occurredAt
) {
}
