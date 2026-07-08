package com.trading.platform.eztrade.market.domain;

/**
 * Enriched summary of a financial instrument.
 * <p>
 * Includes basic information (symbol and name) together with fundamentals such
 * as sector, industry, market capitalization, and P/E ratio.
 */
public record InstrumentOverview(
        String symbol,
        String name,
        String sector,
        String industry,
        long marketCap,
        double peRatio
) {}
