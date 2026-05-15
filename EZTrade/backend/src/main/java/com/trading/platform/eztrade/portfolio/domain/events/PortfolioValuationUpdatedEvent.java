package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de resumen publicado cuando portfolio recalcula la foto agregada de
 * una cartera.
 * <p>
 * Incluye cash disponible proyectado desde wallet, coste base de posiciones
 * abiertas y PnL realizado acumulado.
 */
public record PortfolioValuationUpdatedEvent(
        String owner,
        BigDecimal cashAvailable,
        BigDecimal totalCostBasis,
        BigDecimal totalRealizedPnl,
        LocalDateTime occurredAt
) {
}

