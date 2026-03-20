package com.trading.platform.eztrade.wallet.adapter.in.events;

import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderCancelledUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderExecutedUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderPlacedUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
/**
 * Adaptador de entrada basado en eventos Spring.
 * <p>
 * Escucha eventos del módulo de trading (publicados dentro del mismo proceso mediante Spring events) y delega en los
 * casos de uso del módulo wallet. Mantener el listener separado del servicio permite desacoplar el mecanismo de
 * transporte (Spring events) de la lógica de aplicación.
 */
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
        // Delegación directa al caso de uso.
        handleOrderPlacedUseCase.handle(event);
    }

    @EventListener
    public void on(OrderCancelledEvent event) {
        handleOrderCancelledUseCase.handle(event);
    }

    @EventListener
    public void on(OrderExecutedEvent event) {
        handleOrderExecutedUseCase.handle(event);
    }
}

