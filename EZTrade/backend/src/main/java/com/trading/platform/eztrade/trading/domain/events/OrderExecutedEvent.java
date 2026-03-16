package com.trading.platform.eztrade.trading.domain.events;

import java.math.BigDecimal;

import java.time.LocalDateTime;

/**
 * Evento de dominio emitido cuando una orden se ejecuta.
 *
 * @param orderId identificador de la orden ejecutada
 * @param owner propietario de la orden
 * @param symbol simbolo del activo
 * @param side tipo de orden ejecutada (BUY/SELL)
 * @param quantity cantidad ejecutada
 * @param price precio unitario de ejecucion
 * @param occurredAt fecha y hora de emision del evento
 */
public record OrderExecutedEvent(
        Long orderId,
        String owner,
        String symbol,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        LocalDateTime occurredAt
) {
}
