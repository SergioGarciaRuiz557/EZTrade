package com.trading.platform.eztrade.trading.adapter.in.web.dto;

import com.trading.platform.eztrade.trading.domain.OrderSide;

import java.math.BigDecimal;

/**
 * DTO de entrada para crear una orden desde la API REST.
 *
 * @param symbol simbolo del activo (ticker)
 * @param side tipo de orden (BUY/SELL)
 * @param quantity cantidad solicitada
 * @param price precio unitario ofertado
 */
public record PlaceOrderRequest(
        String symbol,
        OrderSide side,
        BigDecimal quantity,
        BigDecimal price
) {
}
