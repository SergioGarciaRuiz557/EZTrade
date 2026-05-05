package com.trading.platform.eztrade.trading.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de dominio emitido para solicitar la ejecucion de una orden.
 *
 * @param orderId identificador de la orden
 * @param owner propietario de la orden
 * @param symbol simbolo del activo
 * @param side tipo de orden (BUY/SELL)
 * @param quantity cantidad solicitada
 * @param price precio unitario de ejecucion
 * @param occurredAt fecha y hora de emision del evento
 */
public record OrderExecutionRequestEvent(
        Long orderId,
        String owner,
        String symbol,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        LocalDateTime occurredAt
) {
}