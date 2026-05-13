package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.MarketPrice;

import java.time.Instant;

/**
 * DTO de salida REST para el precio actual de mercado.
 * <p>
 * Expone solo los campos que necesita el cliente HTTP y evita serializar el
 * value object {@link com.trading.platform.eztrade.market.domain.Symbol}.
 */
public record MarketPriceResponse(
        String symbol,
        double price,
        Instant timestamp
) {
    /** Convierte el modelo de dominio a una respuesta estable para la API. */
    public static MarketPriceResponse from(MarketPrice price) {
        return new MarketPriceResponse(price.symbol().value(), price.price(), price.timestamp());
    }
}
