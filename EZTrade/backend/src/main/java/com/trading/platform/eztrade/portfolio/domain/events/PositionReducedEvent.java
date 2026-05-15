package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado cuando una SELL reduce una posicion sin cerrarla por
 * completo.
 *
 * @param owner usuario propietario
 * @param symbol simbolo reducido
 * @param quantity cantidad restante tras la venta
 * @param realizedPnlDelta PnL realizado por esta venta concreta
 * @param totalRealizedPnl PnL realizado acumulado de la posicion
 * @param occurredAt momento de publicacion del evento
 */
public record PositionReducedEvent(
        String owner,
        String symbol,
        BigDecimal quantity,
        BigDecimal realizedPnlDelta,
        BigDecimal totalRealizedPnl,
        LocalDateTime occurredAt
) {
}

