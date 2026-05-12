package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.MarketPrice;

import java.time.Instant;

public record MarketPriceResponse(
        String symbol,
        double price,
        Instant timestamp
) {
    public static MarketPriceResponse from(MarketPrice price) {
        return new MarketPriceResponse(price.symbol().value(), price.price(), price.timestamp());
    }
}
