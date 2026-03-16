package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PositionClosedEvent(
        String owner,
        String symbol,
        BigDecimal realizedPnlDelta,
        BigDecimal totalRealizedPnl,
        LocalDateTime occurredAt
) {
}

