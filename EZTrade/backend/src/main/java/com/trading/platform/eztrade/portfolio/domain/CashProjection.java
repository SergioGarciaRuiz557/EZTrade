package com.trading.platform.eztrade.portfolio.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Proyeccion local del cash disponible del usuario, sincronizada desde eventos de wallet.
 * <p>
 * Portfolio no muta dinero real. Este record guarda la ultima cantidad
 * disponible recibida por {@code AvailableCashUpdatedEvent} para componer
 * snapshots de cartera sin llamar directamente al modulo wallet.
 */
public record CashProjection(
        String owner,
        BigDecimal availableCash,
        LocalDateTime updatedAt
) {
}

