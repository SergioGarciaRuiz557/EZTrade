package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PortfolioValuationUpdatedEvent(
        String owner,
        BigDecimal cashAvailable,
        BigDecimal totalCostBasis,
        BigDecimal totalRealizedPnl,
        LocalDateTime occurredAt
) {
}

