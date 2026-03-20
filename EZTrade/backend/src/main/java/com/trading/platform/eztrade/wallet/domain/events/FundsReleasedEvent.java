package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de dominio publicado cuando el wallet <strong>libera</strong> fondos previamente reservados.
 * <p>
 * Suele ocurrir al cancelar una orden o cuando una ejecución requiere menos efectivo del reservado inicialmente.
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

