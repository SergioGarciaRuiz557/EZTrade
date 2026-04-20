package com.trading.platform.eztrade.trading.application.services;

import com.trading.platform.eztrade.trading.application.ports.in.PlaceOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.out.DomainEventPublisherPort;
import com.trading.platform.eztrade.trading.application.ports.out.TradeOrderRepositoryPort;
import com.trading.platform.eztrade.trading.domain.Money;
import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.OrderSide;
import com.trading.platform.eztrade.trading.domain.OrderStatus;
import com.trading.platform.eztrade.trading.domain.Quantity;
import com.trading.platform.eztrade.trading.domain.TradeOrder;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    @Mock
    private TradeOrderRepositoryPort repository;

    @Mock
    private DomainEventPublisherPort eventPublisher;

    @InjectMocks
    private TradingService tradingService;

    @Test
    @DisplayName("place guarda la orden y publica OrderPlacedEvent")
    void place_saves_and_publishes_event() {
        TradeOrder toSave = TradeOrder.place("user@demo.com", "IBM", OrderSide.BUY,
                new Quantity(new BigDecimal("2")), new Money(new BigDecimal("100")));
        TradeOrder saved = toSave.withId(new OrderId(10L));

        given(repository.save(any(TradeOrder.class))).willReturn(saved);
        PlaceOrderUseCase.PlaceOrderCommand command = new PlaceOrderUseCase.PlaceOrderCommand(
                "user@demo.com", "IBM", OrderSide.BUY, new BigDecimal("2"), new BigDecimal("100")
        );

        TradeOrder result = tradingService.place(command);

        assertThat(result.id().value()).isEqualTo(10L);
        verify(repository, times(1)).save(any(TradeOrder.class));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(OrderPlacedEvent.class);
    }

    @Test
    @DisplayName("execute cambia estado a ejecutada y publica evento")
    void execute_updates_status_and_publishes_event() {
        TradeOrder existing = TradeOrder.rehydrate(
                new OrderId(11L),
                "user@demo.com",
                "IBM",
                OrderSide.BUY,
                new Quantity(new BigDecimal("1")),
                new Money(new BigDecimal("50")),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                null
        );

        TradeOrder executed = TradeOrder.rehydrate(
                new OrderId(11L),
                "user@demo.com",
                "IBM",
                OrderSide.BUY,
                new Quantity(new BigDecimal("1")),
                new Money(new BigDecimal("50")),
                OrderStatus.EXECUTED,
                existing.createdAt(),
                LocalDateTime.now()
        );

        given(repository.findById(new OrderId(11L))).willReturn(Optional.of(existing));
        given(repository.save(any(TradeOrder.class))).willReturn(executed);

        TradeOrder result = tradingService.execute(new OrderId(11L));

        assertThat(result.status()).isEqualTo(OrderStatus.EXECUTED);
        verify(eventPublisher).publish(any(OrderExecutedEvent.class));
    }

    @Test
    @DisplayName("execute traduce el fallo de wallet por fondos insuficientes a error de dominio trading")
    void execute_translates_wallet_insufficient_funds_error() {
        TradeOrder existing = TradeOrder.rehydrate(
                new OrderId(12L),
                "user@demo.com",
                "IBM",
                OrderSide.BUY,
                new Quantity(new BigDecimal("2")),
                new Money(new BigDecimal("100")),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                null
        );

        TradeOrder executed = existing.execute();

        given(repository.findById(new OrderId(12L))).willReturn(Optional.of(existing));
        given(repository.save(any(TradeOrder.class))).willReturn(executed);
        doThrow(new RuntimeException("Insufficient wallet funds to execute buy order 12"))
                .when(eventPublisher)
                .publish(any());

        assertThatThrownBy(() -> tradingService.execute(new OrderId(12L)))
                .isInstanceOf(com.trading.platform.eztrade.trading.domain.TradingDomainException.class)
                .hasMessageContaining("Insufficient wallet funds");
    }
}

