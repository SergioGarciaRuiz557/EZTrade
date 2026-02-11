package com.trading.platform.eztrade.market.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representa el precio de mercado actual de un instrumento.
 */
public class MarketPrice {

    private final String instrument;
    private final BigDecimal price;
    private final Instant timestamp;

    public MarketPrice(String instrument, BigDecimal price, Instant timestamp) {
        this.instrument = instrument;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getInstrument() {
        return instrument;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}

