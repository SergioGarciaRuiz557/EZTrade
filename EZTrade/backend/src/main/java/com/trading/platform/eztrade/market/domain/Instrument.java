package com.trading.platform.eztrade.market.domain;

import java.util.Objects;

/**
 * Representa un instrumento financiero (por ejemplo, una acción simulada).
 */
public class Instrument {

    private final String symbol;
    private final String name;
    private final String exchange;

    public Instrument(String symbol, String name, String exchange) {
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getExchange() {
        return exchange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instrument)) return false;
        Instrument that = (Instrument) o;
        return Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }
}

