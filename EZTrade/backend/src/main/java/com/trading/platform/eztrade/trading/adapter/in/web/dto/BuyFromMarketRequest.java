package com.trading.platform.eztrade.trading.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * DTO de entrada para comprar acciones directamente al mercado.
 * <p>
 * No incluye precio porque el backend lo obtiene desde AlphaVantage mediante el
 * modulo market. Asi se evita que el cliente manipule el precio de compra.
 *
 * @param symbol ticker de la accion a comprar
 * @param quantity numero de acciones solicitadas
 */
public record BuyFromMarketRequest(
        String symbol,
        BigDecimal quantity
) {
}
