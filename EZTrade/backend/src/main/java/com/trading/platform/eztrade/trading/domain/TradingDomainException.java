package com.trading.platform.eztrade.trading.domain;

/**
 * Domain exception for trading business-rule violations.
 * <p>
 * Thrown from the aggregate and value objects when an operation does not respect
 * domain invariants.
 */
public class TradingDomainException extends RuntimeException {

    /**
     * Creates the exception with a descriptive message.
     *
     * @param message detail of the violated rule
     */
    public TradingDomainException(String message) {
        super(message);
    }
}
