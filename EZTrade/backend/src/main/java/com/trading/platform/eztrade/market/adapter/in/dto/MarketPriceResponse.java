package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.MarketPrice;

import java.time.Instant;

/**
 * REST output DTO for the current market price.
 * <p>
 * Exposes only the fields required by the HTTP client and avoids serializing
 * the {@link com.trading.platform.eztrade.market.domain.Symbol} value object.
 */
public record MarketPriceResponse(
        String symbol,
        double price,
        Instant timestamp
) {
    /** Converts the domain model into a stable API response. */
    public static MarketPriceResponse from(MarketPrice price) {
        return new MarketPriceResponse(price.symbol().value(), price.price(), price.timestamp());
    }
}
