package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event published when the wallet <strong>settles</strong> an operation
 * associated with an executed order.
 * <p>
 * For a BUY, it is typically emitted with {@code DEBIT} direction (reserved
 * balance is consumed). For a SELL, it is typically emitted with {@code CREDIT}
 * direction (available balance is credited).
 */
public record FundsSettledEvent(
        String orderId,
        String owner,
        BigDecimal amount,
        /** Settlement semantic direction: "DEBIT" or "CREDIT". */
        String direction,
        BigDecimal availableBalance,
        BigDecimal reservedBalance,
        LocalDateTime occurredAt
) {
}

