package com.trading.platform.eztrade.market.domain;

/**
 * Domain exception indicating that an attempt was made to create or use a
 * symbol (ticker) that does not satisfy the established validation rules.
 */
public class InvalidSymbolException extends RuntimeException {
    public InvalidSymbolException(String message) {
        super(message);
    }
}
