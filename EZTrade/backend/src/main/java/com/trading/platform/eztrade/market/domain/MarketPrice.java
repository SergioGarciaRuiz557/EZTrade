package com.trading.platform.eztrade.market.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents the current market price of an instrument.
 */
public record MarketPrice(Symbol symbol, double price, Instant timestamp) {

    public static MarketPrice create(Symbol symbol, double price, Instant timestamp) {
        return new MarketPrice(symbol, price, timestamp);
    }

}

