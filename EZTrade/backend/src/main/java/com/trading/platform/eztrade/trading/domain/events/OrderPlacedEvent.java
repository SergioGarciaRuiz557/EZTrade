package com.trading.platform.eztrade.trading.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event emitted when a new order is registered.
 * <p>
 * Communicates to other modules (without direct coupling) that there is a
 * pending order eligible for subsequent processing.
 *
 * @param orderId created order identifier
 * @param owner order owner
 * @param symbol asset symbol
 * @param side order side (BUY/SELL)
 * @param quantity requested quantity
 * @param price unit price
 * @param occurredAt event emission date and time
 */
public record OrderPlacedEvent(
        Long orderId,
        String owner,
        String symbol,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        LocalDateTime occurredAt
) {
}
