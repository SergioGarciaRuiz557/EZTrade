package com.trading.platform.eztrade.wallet.adapter.in.events;

import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderCancelledUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderExecutedUseCase;
import com.trading.platform.eztrade.wallet.application.ports.in.HandleOrderPlacedUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradingEventsListenerTest {

    @Mock
    private HandleOrderPlacedUseCase handleOrderPlacedUseCase;

    @Mock
    private HandleOrderCancelledUseCase handleOrderCancelledUseCase;

    @Mock
    private HandleOrderExecutedUseCase handleOrderExecutedUseCase;

    private TradingEventsListener listener;

    @BeforeEach
    void setUp() {
        listener = new TradingEventsListener(handleOrderPlacedUseCase, handleOrderCancelledUseCase, handleOrderExecutedUseCase);
    }

    @Test
    @DisplayName("delegates OrderPlacedEvent")
    void delegates_order_placed_event() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                100L,
                "user@demo.com",
                "IBM",
                "BUY",
                new BigDecimal("1"),
                new BigDecimal("100"),
                LocalDateTime.now()
        );

        listener.on(event);

        verify(handleOrderPlacedUseCase).handle(event);
    }

    @Test
    @DisplayName("delegates OrderCancelledEvent")
    void delegates_order_cancelled_event() {
        OrderCancelledEvent event = new OrderCancelledEvent(101L, "user@demo.com", "IBM", LocalDateTime.now());

        listener.on(event);

        verify(handleOrderCancelledUseCase).handle(event);
    }

    @Test
    @DisplayName("delegates OrderExecutedEvent")
    void delegates_order_executed_event() {
        OrderExecutedEvent event = new OrderExecutedEvent(
                102L,
                "user@demo.com",
                "IBM",
                "SELL",
                new BigDecimal("1"),
                new BigDecimal("100"),
                LocalDateTime.now()
        );

        listener.on(event);

        verify(handleOrderExecutedUseCase).handle(event);
    }
}

