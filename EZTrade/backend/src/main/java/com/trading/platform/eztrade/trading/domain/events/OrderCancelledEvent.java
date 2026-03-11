package com.trading.platform.eztrade.trading.domain.events;

import com.trading.platform.eztrade.trading.domain.OrderId;

import java.time.LocalDateTime;

/**
 * Evento de dominio emitido cuando una orden es cancelada.
 *
 * @param orderId identificador de la orden cancelada
 * @param owner propietario de la orden
 * @param symbol simbolo del activo
 * @param occurredAt fecha y hora de emision del evento
 */
public record OrderCancelledEvent(
        OrderId orderId,
        String owner,
        String symbol,
        LocalDateTime occurredAt
) {
}
