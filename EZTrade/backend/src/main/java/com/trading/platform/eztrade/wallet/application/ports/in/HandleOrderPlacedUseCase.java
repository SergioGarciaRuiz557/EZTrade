package com.trading.platform.eztrade.wallet.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;

/**
 * Input port (use case) for reacting when an order is placed.
 * <p>
 * In the wallet, it is used to reserve funds for BUY orders.
 */
public interface HandleOrderPlacedUseCase {

    /**
     * Handles the order-placed event.
     *
     * @param event event published by the trading module
     */
    void handle(OrderPlacedEvent event);
}

