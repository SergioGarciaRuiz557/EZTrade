package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado cuando una BUY aumenta una posicion ya existente.
 *
 * @param owner usuario propietario de la posicion
 * @param symbol simbolo normalizado
 * @param quantity cantidad total tras el incremento
 * @param averageCost nuevo coste medio ponderado
 * @param occurredAt momento de publicacion del evento
 */
public record PositionIncreasedEvent(
        String owner,
        String symbol,
        BigDecimal quantity,
        BigDecimal averageCost,
        LocalDateTime occurredAt
) {
}

