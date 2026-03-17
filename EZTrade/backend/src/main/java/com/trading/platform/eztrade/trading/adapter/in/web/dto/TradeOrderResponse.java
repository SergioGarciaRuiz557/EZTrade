package com.trading.platform.eztrade.trading.adapter.in.web.dto;

import com.trading.platform.eztrade.trading.domain.OrderStatus;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida para exponer una orden en la API REST.
 *
 * @param id id de la orden
 * @param owner propietario
 * @param symbol simbolo del activo
 * @param side tipo de orden en texto
 * @param quantity cantidad
 * @param price precio unitario
 * @param total importe total (precio x cantidad)
 * @param status estado actual
 * @param createdAt fecha de creacion
 * @param executedAt fecha de ejecucion (si aplica)
 */
public record TradeOrderResponse(
        Long id,
        String owner,
        String symbol,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal total,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime executedAt
) {

    /**
     * Convierte una entidad de dominio en DTO de salida.
     *
     * @param order agregado de dominio
     * @return representacion serializable para API
     */
    public static TradeOrderResponse from(TradeOrder order) {
        return new TradeOrderResponse(
                order.id() == null ? null : order.id().value(),
                order.owner(),
                order.symbol(),
                order.side().name(),
                order.quantity().value(),
                order.price().value(),
                order.totalAmount().value(),
                order.status(),
                order.createdAt(),
                order.executedAt()
        );
    }
}
