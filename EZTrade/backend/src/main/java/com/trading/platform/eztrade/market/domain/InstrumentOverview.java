package com.trading.platform.eztrade.market.domain;

/**
 * Resumen enriquecido de un instrumento financiero.
 * <p>
 * Incluye información básica (símbolo y nombre) junto con datos
 * fundamentales como sector, industria, capitalización y PER.
 */
public record InstrumentOverview(
        String symbol,
        String name,
        String sector,
        String industry,
        long marketCap,
        double peRatio
) {}
