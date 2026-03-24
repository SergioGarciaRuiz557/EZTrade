package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de dominio publicado cuando el wallet <strong>reserva</strong> fondos para una orden (normalmente BUY).
 * <p>
 * Informa del importe reservado y de los balances resultantes tras la operación.
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

