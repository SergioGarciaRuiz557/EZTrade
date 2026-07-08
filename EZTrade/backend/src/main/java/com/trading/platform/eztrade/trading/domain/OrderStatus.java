package com.trading.platform.eztrade.trading.domain;

/**
 * Order lifecycle status.
 * <p>
 * The {@link TradeOrder} aggregate transitions between these states according
 * to the module's business rules.
 */
public enum OrderStatus {

    /** Order created and pending execution/cancellation. */
    PENDING,

    /** Successfully executed order. */
    EXECUTED,

    /** Order cancelled by its owner. */
    CANCELLED
}
