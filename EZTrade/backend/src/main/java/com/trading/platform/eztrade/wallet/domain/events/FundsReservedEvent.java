package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FundsReservedEvent(
        String orderId,
        String owner,
        BigDecimal amount,
        BigDecimal availableBalance,
        BigDecimal reservedBalance,
        LocalDateTime occurredAt
) {
}

