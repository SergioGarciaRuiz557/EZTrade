package com.trading.platform.eztrade.notifications.adapter.in.events;

import com.trading.platform.eztrade.notifications.application.ports.in.NotifyOnDomainEventsUseCase;
import com.trading.platform.eztrade.portfolio.domain.events.PortfolioValuationUpdatedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import com.trading.platform.eztrade.wallet.domain.events.InsufficientFundsEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Notifications module input adapter based on Spring events.
 * <p>
 * Its only responsibility is to translate events published inside the process
 * into application use case invocations, keeping the transport (Spring Events)
 * separate from notification business logic.
 */
@Component
public class DomainEventsListener {

    private final NotifyOnDomainEventsUseCase notifyOnDomainEventsUseCase;

    public DomainEventsListener(NotifyOnDomainEventsUseCase notifyOnDomainEventsUseCase) {
        this.notifyOnDomainEventsUseCase = notifyOnDomainEventsUseCase;
    }

    /**
     * Reacts when an order is placed and delegates its processing.
     *
     * @param event event emitted by trading when creating an order
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(OrderPlacedEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }

    /**
     * Reacts when an order is executed and delegates its processing.
     *
     * @param event event emitted by trading when executing an order
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(OrderExecutedEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }

    /**
     * Reacts when an order is canceled and delegates its processing.
     *
     * @param event event emitted by trading when canceling an order
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(OrderCancelledEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }

    /**
     * Reacts when wallet reports insufficient funds.
     *
     * @param event insufficient-funds event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(InsufficientFundsEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }

    /**
     * Reacts when portfolio publishes a new aggregate valuation.
     *
     * @param event portfolio event with cash/cost/pnl data
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(PortfolioValuationUpdatedEvent event) {
        notifyOnDomainEventsUseCase.handle(event);
    }
}

