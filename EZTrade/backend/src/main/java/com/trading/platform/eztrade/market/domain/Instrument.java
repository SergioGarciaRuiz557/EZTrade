package com.trading.platform.eztrade.market.domain;

/**
 * Represents a basic financial instrument returned by market searches.
 * <p>
 * Usually corresponds to a stock, ETF, or other tradable security with a
 * <em>ticker</em>, name, trading region, and currency.
 */
public record Instrument(
        String symbol,
        String name,
        String region,
        String currency
) {}
