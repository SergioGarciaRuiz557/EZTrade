package com.trading.platform.eztrade.trading.application.services;

import com.trading.platform.eztrade.market.api.MarketPriceLookupPort;
import com.trading.platform.eztrade.trading.application.ports.in.BuySellOfferUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.PlaceOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.PlaceSellOfferUseCase;
import com.trading.platform.eztrade.trading.application.ports.out.DomainEventPublisherPort;
import com.trading.platform.eztrade.trading.application.ports.out.TradeOrderRepositoryPort;
import com.trading.platform.eztrade.trading.domain.Money;
import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.OrderSide;
import com.trading.platform.eztrade.trading.domain.OrderStatus;
import com.trading.platform.eztrade.trading.domain.Quantity;
import com.trading.platform.eztrade.trading.domain.TradeOrder;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutionRequestEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

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

    @Mock
    private MarketPriceLookupPort marketPriceLookupPort;

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
    @DisplayName("execute cambia estado a ejecutada y publica evento de solicitud de ejecucion")
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
        verify(eventPublisher).publish(any(OrderExecutionRequestEvent.class));
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

    @Test
    @DisplayName("placeSellOffer rechaza precios por encima del mercado actual")
    void placeSellOffer_rejects_price_above_current_market_price() {
        given(marketPriceLookupPort.currentPrice("IBM")).willReturn(new BigDecimal("100"));

        PlaceSellOfferUseCase.PlaceSellOfferCommand command = new PlaceSellOfferUseCase.PlaceSellOfferCommand(
                "seller@demo.com",
                "IBM",
                new BigDecimal("1"),
                new BigDecimal("101")
        );

        assertThatThrownBy(() -> tradingService.placeSellOffer(command))
                .isInstanceOf(com.trading.platform.eztrade.trading.domain.TradingDomainException.class)
                .hasMessageContaining("precio de venta no puede superar");
    }

    @Test
    @DisplayName("placeSellOffer guarda una oferta SELL si hay acciones y el precio no supera mercado")
    void placeSellOffer_saves_pending_sell_offer_when_position_is_available() {
        given(marketPriceLookupPort.currentPrice("IBM")).willReturn(new BigDecimal("100"));
        given(repository.findExecutedOrdersByOwnerAndSymbol("seller@demo.com", "IBM"))
                .willReturn(List.of(TradeOrder.rehydrate(
                        new OrderId(19L),
                        "seller@demo.com",
                        "IBM",
                        OrderSide.BUY,
                        new Quantity(new BigDecimal("5")),
                        new Money(new BigDecimal("80")),
                        OrderStatus.EXECUTED,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )));
        given(repository.findPendingSellOffersByOwnerAndSymbol("seller@demo.com", "IBM"))
                .willReturn(List.of());
        given(repository.save(any(TradeOrder.class)))
                .willAnswer(i -> ((TradeOrder) i.getArgument(0)).withId(new OrderId(20L)));

        TradeOrder result = tradingService.placeSellOffer(new PlaceSellOfferUseCase.PlaceSellOfferCommand(
                "seller@demo.com",
                "IBM",
                new BigDecimal("2"),
                new BigDecimal("95")
        ));

        assertThat(result.id().value()).isEqualTo(20L);
        assertThat(result.side()).isEqualTo(OrderSide.SELL);
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        verify(eventPublisher).publish(any(OrderPlacedEvent.class));
    }

    @Test
    @DisplayName("buyOffer ejecuta una compra para comprador y una venta para vendedor")
    void buyOffer_executes_buyer_and_seller_orders() {
        TradeOrder sellOffer = TradeOrder.rehydrate(
                new OrderId(30L),
                "seller@demo.com",
                "IBM",
                OrderSide.SELL,
                new Quantity(new BigDecimal("2")),
                new Money(new BigDecimal("90")),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                null
        );
        Map<Long, TradeOrder> orders = new HashMap<>();
        orders.put(30L, sellOffer);
        AtomicLong nextOrderId = new AtomicLong(31L);

        given(repository.findByIdForUpdate(new OrderId(30L))).willReturn(Optional.of(sellOffer));
        given(marketPriceLookupPort.currentPrice("IBM")).willReturn(new BigDecimal("100"));
        given(repository.findExecutedOrdersByOwnerAndSymbol("seller@demo.com", "IBM"))
                .willReturn(List.of(TradeOrder.rehydrate(
                        new OrderId(29L),
                        "seller@demo.com",
                        "IBM",
                        OrderSide.BUY,
                        new Quantity(new BigDecimal("5")),
                        new Money(new BigDecimal("80")),
                        OrderStatus.EXECUTED,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )));
        given(repository.findPendingSellOffersByOwnerAndSymbol("seller@demo.com", "IBM"))
                .willReturn(List.of(sellOffer));
        given(repository.findById(any(OrderId.class)))
                .willAnswer(i -> Optional.ofNullable(orders.get(((OrderId) i.getArgument(0)).value())));
        given(repository.save(any(TradeOrder.class))).willAnswer(i -> {
            TradeOrder order = i.getArgument(0);
            TradeOrder saved = order.id() == null ? order.withId(new OrderId(nextOrderId.getAndIncrement())) : order;
            orders.put(saved.id().value(), saved);
            return saved;
        });

        BuySellOfferUseCase.MarketplaceTradeResult result = tradingService.buyOffer(
                new BuySellOfferUseCase.BuySellOfferCommand("buyer@demo.com", 30L)
        );

        assertThat(result.buyerOrder().owner()).isEqualTo("buyer@demo.com");
        assertThat(result.buyerOrder().side()).isEqualTo(OrderSide.BUY);
        assertThat(result.buyerOrder().status()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(result.sellerOrder().owner()).isEqualTo("seller@demo.com");
        assertThat(result.sellerOrder().side()).isEqualTo(OrderSide.SELL);
        assertThat(result.sellerOrder().status()).isEqualTo(OrderStatus.EXECUTED);
        verify(eventPublisher, times(3)).publish(any());
    }
}
