package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event published when the wallet <strong>reserves</strong> funds for an
 * order (usually BUY).
 * <p>
 * Reports the reserved amount and the resulting balances after the operation.
 */
public record FundsReservedEvent(
        String orderId,
        String owner,
        BigDecimal amount,
        BigDecimal availableBalance,
        BigDecimal reservedBalance,
        LocalDateTime occurredAt
) {
}

