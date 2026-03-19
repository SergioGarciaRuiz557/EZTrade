package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InsufficientFundsEvent(
        String orderId,
        String owner,
        BigDecimal requestedAmount,
        BigDecimal availableBalance,
        BigDecimal reservedBalance,
        String reason,
        LocalDateTime occurredAt
) {
}

