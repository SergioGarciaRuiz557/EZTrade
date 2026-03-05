package com.trading.platform.eztrade.market.domain;

/**
 * Representa un instrumento financiero básico devuelto por las búsquedas en el mercado.
 * <p>
 * Normalmente se corresponde con una acción, ETF u otro valor negociable
 * que tiene un <em>ticker</em>, un nombre, una región de cotización y una moneda.
 */
public record Instrument(
        String symbol,
        String name,
        String region,
        String currency
) {}
