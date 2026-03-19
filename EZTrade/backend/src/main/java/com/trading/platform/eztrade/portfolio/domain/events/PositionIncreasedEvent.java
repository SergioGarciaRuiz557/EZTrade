package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PositionIncreasedEvent(
        String owner,
        String symbol,
        BigDecimal quantity,
        BigDecimal averageCost,
        LocalDateTime occurredAt
) {
}

