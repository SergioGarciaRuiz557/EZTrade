package com.trading.platform.eztrade.portfolio.adapter.in.events;

import com.trading.platform.eztrade.portfolio.application.ports.in.HandleOrderExecutedUseCase;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Input adapter that translates trading events into portfolio use cases.
 */
@Component
public class TradingEventsListener {

    private final HandleOrderExecutedUseCase handleOrderExecutedUseCase;

    public TradingEventsListener(HandleOrderExecutedUseCase handleOrderExecutedUseCase) {
        this.handleOrderExecutedUseCase = handleOrderExecutedUseCase;
    }

    @EventListener
    public void on(OrderExecutedEvent event) {
        handleOrderExecutedUseCase.handle(event);
    }
}

