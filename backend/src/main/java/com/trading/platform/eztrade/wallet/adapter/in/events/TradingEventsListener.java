package com.trading.platform.eztrade.wallet.adapter.in.events;

import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutionRequestEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderCancelledUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderExecutedUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderPlacedUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


/**
 * Input adapter based on Spring events.
 * <p>
 * Listens to trading module events (published within the same process through
 * Spring events) and delegates to wallet module use cases. Keeping the listener
 * separate from the service decouples the transport mechanism (Spring events)
 * from application logic.
 */
@Component("TradingEventsListener")
public class TradingEventsListener {

    private final HandleOrderPlacedUseCase handleOrderPlacedUseCase;
    private final HandleOrderCancelledUseCase handleOrderCancelledUseCase;
    private final HandleOrderExecutedUseCase handleOrderExecutedUseCase;

    public TradingEventsListener(HandleOrderPlacedUseCase handleOrderPlacedUseCase,
                                 HandleOrderCancelledUseCase handleOrderCancelledUseCase,
                                 HandleOrderExecutedUseCase handleOrderExecutedUseCase) {
        this.handleOrderPlacedUseCase = handleOrderPlacedUseCase;
        this.handleOrderCancelledUseCase = handleOrderCancelledUseCase;
        this.handleOrderExecutedUseCase = handleOrderExecutedUseCase;
    }

    @EventListener
    public void on(OrderPlacedEvent event) {
        // Direct delegation to the use case.
        // Intentionally synchronous: if wallet cannot reserve funds, trading
        // must abort the transaction and avoid leaving an unsupported pending BUY.
        handleOrderPlacedUseCase.handle(event);
    }

    @EventListener
    public void on(OrderCancelledEvent event) {
        handleOrderCancelledUseCase.handle(event);
    }

    @EventListener
    public void on(OrderExecutionRequestEvent event) {
        handleOrderExecutedUseCase.handle(event);
    }
}
