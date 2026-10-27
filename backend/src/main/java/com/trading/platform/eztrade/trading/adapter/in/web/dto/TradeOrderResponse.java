package com.trading.platform.eztrade.trading.adapter.in.web.dto;

import com.trading.platform.eztrade.trading.domain.OrderStatus;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Output DTO for exposing an order through the REST API.
 *
 * @param id order id
 * @param owner owner
 * @param symbol asset symbol
 * @param side order side as text
 * @param quantity quantity
 * @param price unit price
 * @param total total amount (price x quantity)
 * @param status current status
 * @param createdAt creation timestamp
 * @param executedAt execution timestamp, when applicable
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
     * Converts a domain entity into an output DTO.
     *
     * @param order domain aggregate
     * @return API-serializable representation
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
