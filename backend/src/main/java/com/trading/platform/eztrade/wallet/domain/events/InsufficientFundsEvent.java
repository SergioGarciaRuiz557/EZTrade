package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event published when the wallet cannot complete an operation due to
 * <strong>insufficient funds</strong>.
 * <p>
 * Used to notify the trading module (or other consumers) that an order could
 * not be reserved or settled due to lack of balance. Includes the current wallet
 * state and a textual reason.
 */
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

