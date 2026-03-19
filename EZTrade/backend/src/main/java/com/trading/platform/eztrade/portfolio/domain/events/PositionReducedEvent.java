package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PositionReducedEvent(
        String owner,
        String symbol,
        BigDecimal quantity,
        BigDecimal realizedPnlDelta,
        BigDecimal totalRealizedPnl,
        LocalDateTime occurredAt
) {
}

