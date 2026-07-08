package com.trading.platform.eztrade.portfolio.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Local projection of the user's available cash, synchronized from wallet events.
 * <p>
 * Portfolio does not mutate real money. This record stores the latest available
 * amount received through {@code AvailableCashUpdatedEvent} to compose
 * portfolio snapshots without calling the wallet module directly.
 */
public record CashProjection(
        String owner,
        BigDecimal availableCash,
        LocalDateTime updatedAt
) {
}

