package com.trading.platform.eztrade.notifications.adapter.in.events;

import com.trading.platform.eztrade.notifications.application.ports.in.NotifyOnDomainEventsUseCase;
import com.trading.platform.eztrade.portfolio.domain.events.PortfolioValuationUpdatedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
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
class DomainEventsListenerTest {

    @Mock
    private NotifyOnDomainEventsUseCase notifyOnDomainEventsUseCase;

    private DomainEventsListener listener;

    @BeforeEach
    void setUp() {
        listener = new DomainEventsListener(notifyOnDomainEventsUseCase);
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

        verify(notifyOnDomainEventsUseCase).handle(event);
    }

    @Test
    @DisplayName("delegates OrderExecutedEvent")
    void delegates_order_executed_event() {
        OrderExecutedEvent event = new OrderExecutedEvent(
                101L,
                "user@demo.com",
                "IBM",
                "SELL",
                new BigDecimal("1"),
                new BigDecimal("100"),
                LocalDateTime.now()
        );

        listener.on(event);

        verify(notifyOnDomainEventsUseCase).handle(event);
    }

    @Test
    @DisplayName("delegates OrderCancelledEvent")
    void delegates_order_cancelled_event() {
        OrderCancelledEvent event = new OrderCancelledEvent(102L, "user@demo.com", "IBM", LocalDateTime.now());

        listener.on(event);

        verify(notifyOnDomainEventsUseCase).handle(event);
    }

    @Test
    @DisplayName("delegates PortfolioValuationUpdatedEvent")
    void delegates_portfolio_event() {
        PortfolioValuationUpdatedEvent event = new PortfolioValuationUpdatedEvent(
                "user@demo.com",
                new BigDecimal("100"),
                new BigDecimal("50"),
                new BigDecimal("10"),
                LocalDateTime.now()
        );

        listener.on(event);

        verify(notifyOnDomainEventsUseCase).handle(event);
    }
}

