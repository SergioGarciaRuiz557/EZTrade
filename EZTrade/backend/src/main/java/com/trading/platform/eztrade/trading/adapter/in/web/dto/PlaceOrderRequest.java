package com.trading.platform.eztrade.trading.adapter.in.web.dto;

import com.trading.platform.eztrade.trading.domain.OrderSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
        @NotBlank(message = "Symbol is required")
        String symbol,
        @NotNull(message = "Order side is required")
        OrderSide side,
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than zero")
        BigDecimal quantity,
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        BigDecimal price
) {
}
