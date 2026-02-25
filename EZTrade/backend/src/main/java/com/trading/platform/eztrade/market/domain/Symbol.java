package com.trading.platform.eztrade.market.domain;


public record Symbol(String value) {


    public Symbol {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidSymbolException("Ticker cannot be empty");
        }

        if (!value.matches("^[A-Z]{1,5}$")) {
            throw new InvalidSymbolException("Invalid ticker: " + value);
        }
    }


    public static Symbol of(String value) {
        return new Symbol(value);
    }
}
