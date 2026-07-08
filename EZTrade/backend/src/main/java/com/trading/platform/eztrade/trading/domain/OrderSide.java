package com.trading.platform.eztrade.trading.domain;

/**
 * Order type within the trading domain.
 * <p>
 * Defines the economic direction of the operation: buy ({@link #BUY}) or sell ({@link #SELL}).
 */
public enum OrderSide {

    /** Asset buy order. */
    BUY,

    /** Asset sell order. */
    SELL
}
