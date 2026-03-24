package com.trading.platform.eztrade.trading.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento de dominio emitido cuando se registra una nueva orden.
 * <p>
 * Sirve para comunicar a otros modulos (sin acoplamiento directo) que existe
 * una orden pendiente susceptible de procesos posteriores.
 *
 * @param orderId identificador de la orden creada
 * @param owner propietario de la orden
 * @param symbol simbolo del activo
 * @param side tipo de orden (BUY/SELL)
 * @param quantity cantidad solicitada
 * @param price precio unitario
 * @param occurredAt fecha y hora de emision del evento
 */
public record OrderPlacedEvent(
        Long orderId,
        String owner,
        String symbol,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        LocalDateTime occurredAt
) {
}
