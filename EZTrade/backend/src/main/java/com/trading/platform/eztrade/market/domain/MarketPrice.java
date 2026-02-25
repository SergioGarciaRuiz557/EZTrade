package com.trading.platform.eztrade.market.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representa el precio de mercado actual de un instrumento.
 */
public record MarketPrice(Symbol symbol, double price, Instant timestamp) {

    public static MarketPrice create(Symbol symbol, double price, Instant timestamp) {
        return new MarketPrice(symbol, price, timestamp);
    }

}

