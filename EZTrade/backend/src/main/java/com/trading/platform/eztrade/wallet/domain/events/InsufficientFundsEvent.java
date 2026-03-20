package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de dominio publicado cuando el wallet no puede completar una operación por <strong>fondos insuficientes</strong>.
 * <p>
 * Se usa para notificar al módulo de trading (u otros consumidores) de que no se ha podido reservar o liquidar una
 * orden por falta de saldo. Incluye el estado actual del wallet y una razón textual.
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

