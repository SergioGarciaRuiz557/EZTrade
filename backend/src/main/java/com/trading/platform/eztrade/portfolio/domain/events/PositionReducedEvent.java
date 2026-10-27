package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a SELL reduces a position without fully closing it.
 *
 * @param owner owning user
 * @param symbol reduced symbol
 * @param quantity remaining quantity after the sale
 * @param realizedPnlDelta realized PnL for this specific sale
 * @param totalRealizedPnl accumulated realized PnL for the position
 * @param occurredAt event publication timestamp
 */
public record PositionReducedEvent(
        String owner,
        String symbol,
        BigDecimal quantity,
        BigDecimal realizedPnlDelta,
        BigDecimal totalRealizedPnl,
        LocalDateTime occurredAt
) {
}

