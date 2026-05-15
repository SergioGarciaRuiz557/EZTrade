package com.trading.platform.eztrade.portfolio.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado cuando portfolio crea la primera posicion de un usuario en
 * un simbolo tras ejecutar una BUY.
 *
 * @param owner usuario propietario de la posicion
 * @param symbol simbolo normalizado
 * @param quantity cantidad abierta
 * @param averageCost coste medio inicial
 * @param occurredAt momento de publicacion del evento
 */
public record PositionOpenedEvent(
        String owner,
        String symbol,
        BigDecimal quantity,
        BigDecimal averageCost,
        LocalDateTime occurredAt
) {
}

