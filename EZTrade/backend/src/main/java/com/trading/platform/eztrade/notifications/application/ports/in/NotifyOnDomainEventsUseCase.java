package com.trading.platform.eztrade.notifications.application.ports.in;

import com.trading.platform.eztrade.portfolio.domain.events.PortfolioValuationUpdatedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import com.trading.platform.eztrade.wallet.domain.events.InsufficientFundsEvent;

/**
 * Notifications input port for processing domain events.
 * <p>
 * This contract defines which external events can trigger notifications.
 * Implementations must be limited to building and dispatching messages without
 * introducing business logic from the emitting modules.
 */
public interface NotifyOnDomainEventsUseCase {

    /**
     * Processes order placement and generates the corresponding notification.
     *
     * @param event order placed event
     */
    void handle(OrderPlacedEvent event);

    /**
     * Processes order execution and generates the corresponding notification.
     *
     * @param event order executed event
     */
    void handle(OrderExecutedEvent event);

    /**
     * Processes order cancellation and generates the corresponding notification.
     *
     * @param event order canceled event
     */
    void handle(OrderCancelledEvent event);

    /**
     * Processes an insufficient-funds event from wallet.
     *
     * @param event wallet event with insufficient-funds details
     */
    void handle(InsufficientFundsEvent event);

    /**
     * Processes a portfolio valuation update.
     *
     * @param event portfolio event with aggregate metrics
     */
    void handle(PortfolioValuationUpdatedEvent event);
}
