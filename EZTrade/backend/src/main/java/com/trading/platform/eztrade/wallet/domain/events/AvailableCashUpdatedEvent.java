package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published by the wallet to share a user's updated available cash.
 */
public record AvailableCashUpdatedEvent(
        String owner,
        BigDecimal availableCash,
        String trigger,
        String referenceId,
        LocalDateTime occurredAt
) {
}

