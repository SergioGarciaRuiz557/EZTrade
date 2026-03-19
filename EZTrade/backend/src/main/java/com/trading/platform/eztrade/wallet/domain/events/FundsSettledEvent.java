package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FundsSettledEvent(
        String orderId,
        String owner,
        BigDecimal amount,
        String direction,
        BigDecimal availableBalance,
        BigDecimal reservedBalance,
        LocalDateTime occurredAt
) {
}

