package com.trading.platform.eztrade.portfolio.application.ports.in;

import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;

/**
 * Input port that processes executions confirmed by wallet/trading.
 * <p>
 * Portfolio listens to this event to open, increase, reduce, or close positions
 * without coupling itself to the internal trading service.
 */
public interface HandleOrderExecutedUseCase {

    /** Applies the executed order effect to the user's positions. */
    void handle(OrderExecutedEvent event);
}

