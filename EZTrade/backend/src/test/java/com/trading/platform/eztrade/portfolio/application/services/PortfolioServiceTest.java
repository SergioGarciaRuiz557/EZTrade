package com.trading.platform.eztrade.portfolio.application.services;

import com.trading.platform.eztrade.market.api.MarketPriceLookupPort;
import com.trading.platform.eztrade.portfolio.application.ports.out.DomainEventPublisherPort;
import com.trading.platform.eztrade.portfolio.application.ports.out.PositionRepositoryPort;
import com.trading.platform.eztrade.portfolio.application.ports.out.CashProjectionRepositoryPort;
import com.trading.platform.eztrade.portfolio.domain.CashProjection;
import com.trading.platform.eztrade.portfolio.domain.PortfolioSnapshot;
import com.trading.platform.eztrade.portfolio.domain.Position;
import com.trading.platform.eztrade.portfolio.domain.events.PortfolioValuationUpdatedEvent;
import com.trading.platform.eztrade.portfolio.domain.events.PositionClosedEvent;
import com.trading.platform.eztrade.portfolio.domain.events.PositionOpenedEvent;
import com.trading.platform.eztrade.portfolio.domain.events.PositionReducedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.wallet.domain.events.AvailableCashUpdatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PositionRepositoryPort positionRepository;

    @Mock
    private CashProjectionRepositoryPort cashProjectionRepository;

    @Mock
    private DomainEventPublisherPort eventPublisher;

    @Mock
    private MarketPriceLookupPort marketPriceLookupPort;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    @DisplayName("BUY abre posicion y publica valoracion con cash de la proyeccion local")
    void buy_opens_position_and_publishes_valuation_with_projected_cash() {
        OrderExecutedEvent event = new OrderExecutedEvent(
                10L,
                "user@demo.com",
                "IBM",
                "BUY",
                new BigDecimal("2"),
                new BigDecimal("100"),
                null
        );

        given(positionRepository.findByOwnerAndSymbol("user@demo.com", "IBM")).willReturn(Optional.empty());
        given(positionRepository.save(any(Position.class))).willAnswer(i -> i.getArgument(0));
        given(positionRepository.findByOwner("user@demo.com")).willReturn(List.of(
                Position.open("user@demo.com", "IBM", new BigDecimal("2"), new BigDecimal("100"))
        ));
        given(cashProjectionRepository.findByOwner("user@demo.com"))
                .willReturn(Optional.of(new CashProjection("user@demo.com", new BigDecimal("500"), LocalDateTime.now())));

        portfolioService.handle(event);

        verify(positionRepository, times(1)).save(any(Position.class));
        verify(cashProjectionRepository, times(1)).findByOwner("user@demo.com");
        verify(marketPriceLookupPort, never()).currentPrice(any());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeastOnce()).publish(captor.capture());
        assertThat(captor.getAllValues()).anyMatch(PositionOpenedEvent.class::isInstance);
        assertThat(captor.getAllValues()).anyMatch(PortfolioValuationUpdatedEvent.class::isInstance);
    }

    @Test
    @DisplayName("SELL reduce posicion y publica PositionReducedEvent")
    void sell_reduces_position_and_publishes_reduced_event() {
        OrderExecutedEvent event = new OrderExecutedEvent(
                11L,
                "user@demo.com",
                "IBM",
                "SELL",
                new BigDecimal("1"),
                new BigDecimal("120"),
                null
        );

        Position current = Position.open("user@demo.com", "IBM", new BigDecimal("2"), new BigDecimal("100"));

        given(positionRepository.findByOwnerAndSymbol("user@demo.com", "IBM")).willReturn(Optional.of(current));
        given(positionRepository.save(any(Position.class))).willAnswer(i -> i.getArgument(0));
        given(positionRepository.findByOwner("user@demo.com")).willReturn(List.of(
                current.reduce(new BigDecimal("1"), new BigDecimal("120")).position()
        ));
        given(cashProjectionRepository.findByOwner("user@demo.com"))
                .willReturn(Optional.of(new CashProjection("user@demo.com", new BigDecimal("120"), LocalDateTime.now())));

        portfolioService.handle(event);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeastOnce()).publish(captor.capture());
        assertThat(captor.getAllValues()).anyMatch(PositionReducedEvent.class::isInstance);
        verify(marketPriceLookupPort, never()).currentPrice(any());
    }

    @Test
    @DisplayName("getByOwner mantiene PnL realizado del portfolio y calcula PnL de posicion con precio de mercado")
    void getByOwner_keeps_portfolio_realized_pnl_and_enriches_positions_with_market_pnl() {
        Position position = Position.rehydrate(
                "user@demo.com",
                "IBM",
                new BigDecimal("2"),
                new BigDecimal("120"),
                new BigDecimal("15"),
                LocalDateTime.now()
        );
        Position closedPosition = Position.rehydrate(
                "user@demo.com",
                "AAPL",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("20"),
                LocalDateTime.now()
        );

        given(positionRepository.findByOwner("user@demo.com")).willReturn(List.of(position, closedPosition));
        given(cashProjectionRepository.findByOwner("user@demo.com"))
                .willReturn(Optional.of(new CashProjection("user@demo.com", new BigDecimal("500"), LocalDateTime.now())));
        given(marketPriceLookupPort.currentPrice("IBM")).willReturn(new BigDecimal("140"));

        PortfolioSnapshot snapshot = portfolioService.getByOwner("user@demo.com");

        assertThat(snapshot.totalRealizedPnl()).isEqualByComparingTo("35");
        assertThat(snapshot.positions()).containsExactly(position);
        assertThat(snapshot.marketValuationFor("IBM")).hasValueSatisfying(valuation -> {
            assertThat(valuation.currentPrice()).isEqualByComparingTo("140");
            assertThat(valuation.marketValue()).isEqualByComparingTo("280");
            assertThat(valuation.unrealizedPnl()).isEqualByComparingTo("40");
        });
        assertThat(snapshot.marketValuationFor("AAPL")).isEmpty();
    }

    @Test
    @DisplayName("SELL que cierra posicion guarda el PnL realizado para que siga contando en el portfolio")
    void sell_closing_position_saves_closed_position_realized_pnl() {
        OrderExecutedEvent event = new OrderExecutedEvent(
                12L,
                "user@demo.com",
                "IBM",
                "SELL",
                new BigDecimal("2"),
                new BigDecimal("120"),
                null
        );

        Position current = Position.open("user@demo.com", "IBM", new BigDecimal("2"), new BigDecimal("100"));

        given(positionRepository.findByOwnerAndSymbol("user@demo.com", "IBM")).willReturn(Optional.of(current));
        given(positionRepository.save(any(Position.class))).willAnswer(i -> i.getArgument(0));
        given(positionRepository.findByOwner("user@demo.com")).willReturn(List.of(
                current.reduce(new BigDecimal("2"), new BigDecimal("120")).position()
        ));
        given(cashProjectionRepository.findByOwner("user@demo.com"))
                .willReturn(Optional.of(new CashProjection("user@demo.com", new BigDecimal("240"), LocalDateTime.now())));

        portfolioService.handle(event);

        ArgumentCaptor<Position> positionCaptor = ArgumentCaptor.forClass(Position.class);
        verify(positionRepository, times(1)).save(positionCaptor.capture());
        assertThat(positionCaptor.getValue().isClosed()).isTrue();
        assertThat(positionCaptor.getValue().realizedPnl()).isEqualByComparingTo("40");

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeastOnce()).publish(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).anyMatch(PositionClosedEvent.class::isInstance);
    }

    @Test
    @DisplayName("evento de wallet actualiza la proyeccion de cash")
    void wallet_cash_event_updates_projection() {
        AvailableCashUpdatedEvent event = new AvailableCashUpdatedEvent(
                "user@demo.com",
                new BigDecimal("350"),
                "ORDER_EXECUTED",
                "20",
                LocalDateTime.now()
        );

        given(cashProjectionRepository.save(any(CashProjection.class))).willAnswer(i -> i.getArgument(0));

        portfolioService.handle(event);

        ArgumentCaptor<CashProjection> captor = ArgumentCaptor.forClass(CashProjection.class);
        verify(cashProjectionRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().owner()).isEqualTo("user@demo.com");
        assertThat(captor.getValue().availableCash()).isEqualByComparingTo("350");
    }
}

