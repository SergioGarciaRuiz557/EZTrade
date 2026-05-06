package com.trading.platform.eztrade.trading.adapter.in.web.dto;

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
        String symbol,
        BigDecimal quantity,
        BigDecimal price
) {
}
