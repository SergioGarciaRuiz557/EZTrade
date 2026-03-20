package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de dominio publicado cuando el wallet <strong>liquida</strong> una operación asociada a una orden ejecutada.
 * <p>
 * Para una BUY se emite típicamente con dirección {@code DEBIT} (se consume reservado).
 * Para una SELL se emite típicamente con dirección {@code CREDIT} (se abona disponible).
 */
public record FundsSettledEvent(
        String orderId,
        String owner,
        BigDecimal amount,
        /** Dirección semántica de la liquidación: "DEBIT" o "CREDIT". */
        String direction,
        BigDecimal availableBalance,
        BigDecimal reservedBalance,
        LocalDateTime occurredAt
) {
}

