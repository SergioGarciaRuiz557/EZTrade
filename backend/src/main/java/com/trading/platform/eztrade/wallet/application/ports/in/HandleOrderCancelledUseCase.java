package com.trading.platform.eztrade.wallet.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;

/**
 * Input port (use case) for reacting when an order is cancelled.
 * <p>
 * In the wallet, it is used to release the reserved funds associated with that order.
 */
public interface HandleOrderCancelledUseCase {

    /**
     * Handles the order-cancelled event.
     *
     * @param event event published by the trading module
     */
    void handle(OrderCancelledEvent event);
}

