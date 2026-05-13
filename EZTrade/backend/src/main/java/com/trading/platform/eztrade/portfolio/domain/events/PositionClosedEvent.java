package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado cuando una SELL deja la posicion a cantidad cero.
 *
 * @param owner usuario propietario
 * @param symbol simbolo cerrado
 * @param realizedPnlDelta PnL generado por la venta que cierra
 * @param totalRealizedPnl PnL realizado acumulado tras el cierre
 * @param occurredAt momento de publicacion del evento
 */
public record PositionClosedEvent(
        String owner,
        String symbol,
        BigDecimal realizedPnlDelta,
        BigDecimal totalRealizedPnl,
        LocalDateTime occurredAt
) {
}

