package com.trading.platform.eztrade.trading.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de entrada para publicar acciones en venta dentro de la plataforma.
 * <p>
 * El precio indicado por el usuario se valida en la capa de aplicacion para que
 * no supere el precio actual de mercado.
 *
 * @param symbol ticker de la accion ofrecida
 * @param quantity numero de acciones que se ponen a la venta
 * @param price precio unitario deseado por el vendedor
 */
public record PlaceSellOfferRequest(
        @NotBlank(message = "Symbol is required")
        String symbol,
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than zero")
        BigDecimal quantity,
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        BigDecimal price
) {
}
