package com.trading.platform.eztrade.wallet.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado por wallet para compartir el efectivo disponible actualizado de un usuario.
 */
public record AvailableCashUpdatedEvent(
        String owner,
        BigDecimal availableCash,
        String trigger,
        String referenceId,
        LocalDateTime occurredAt
) {
}

