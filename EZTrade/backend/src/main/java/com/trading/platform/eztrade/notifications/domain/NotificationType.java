package com.trading.platform.eztrade.notifications.domain;

/**
 * Functional notification types for classification and traceability.
 * <p>
 * Used to label the message across all channels and support filtering, metrics,
 * auditability, and future per-type user preferences.
 */
public enum NotificationType {
    /** Notification emitted when an order is placed. */
    ORDER_PLACED,
    /** Notification emitted when an order is executed. */
    ORDER_EXECUTED,
    /** Notification emitted when an order is canceled. */
    ORDER_CANCELLED,
    /** Notification emitted when wallet cannot cover an operation. */
    INSUFFICIENT_FUNDS,
    /** Notification emitted when portfolio recalculates its aggregate valuation. */
    PORTFOLIO_VALUATION_UPDATED
}
