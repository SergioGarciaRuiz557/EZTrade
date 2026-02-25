package com.trading.platform.eztrade.market.domain;

public class InvalidSymbolException extends RuntimeException {
    public InvalidSymbolException(String message) {
        super(message);
    }
}
