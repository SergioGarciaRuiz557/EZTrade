package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event published when the wallet <strong>releases</strong> previously
 * reserved funds.
 * <p>
 * Usually occurs when cancelling an order or when an execution requires less
 * cash than was initially reserved.
 */
public record FundsReleasedEvent(
        String orderId,
        String owner,
        BigDecimal amount,
        BigDecimal availableBalance,
        BigDecimal reservedBalance,
        LocalDateTime occurredAt
) {
}

