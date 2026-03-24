package com.trading.platform.eztrade.trading.adapter.in.web;

import com.trading.platform.eztrade.trading.application.ports.in.CancelOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.ExecuteOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.GetOrdersUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.PlaceOrderUseCase;
import com.trading.platform.eztrade.trading.domain.Money;
import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.OrderSide;
import com.trading.platform.eztrade.trading.domain.OrderStatus;
import com.trading.platform.eztrade.trading.domain.Quantity;
import com.trading.platform.eztrade.trading.domain.TradeOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TradingControllerSecurityTest {

    @Mock
    private PlaceOrderUseCase placeOrderUseCase;

    @Mock
    private ExecuteOrderUseCase executeOrderUseCase;

    @Mock
    private CancelOrderUseCase cancelOrderUseCase;

    @Mock
    private GetOrdersUseCase getOrdersUseCase;

    @Mock
    private Authentication authentication;

    @Test
    @DisplayName("getById devuelve forbidden cuando la orden pertenece a otro usuario")
    void getById_forbidden_whenOrderBelongsToAnotherUser() {
        TradingController controller = new TradingController(placeOrderUseCase, executeOrderUseCase, cancelOrderUseCase, getOrdersUseCase);
        TradeOrder foreignOrder = TradeOrder.rehydrate(
                new OrderId(99L),
                "owner@demo.com",
                "IBM",
                OrderSide.BUY,
                new Quantity(new BigDecimal("1")),
                new Money(new BigDecimal("100")),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                null
        );

        given(authentication.getName()).willReturn("intruder@demo.com");
        given(getOrdersUseCase.getById(new OrderId(99L))).willReturn(foreignOrder);

        assertThatThrownBy(() -> controller.getById(99L, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("another user's order");

        verify(getOrdersUseCase).getById(new OrderId(99L));
        verifyNoInteractions(executeOrderUseCase);
    }

    @Test
    @DisplayName("execute devuelve forbidden cuando la orden pertenece a otro usuario")
    void execute_forbidden_whenOrderBelongsToAnotherUser() {
        TradingController controller = new TradingController(placeOrderUseCase, executeOrderUseCase, cancelOrderUseCase, getOrdersUseCase);
        TradeOrder foreignOrder = TradeOrder.rehydrate(
                new OrderId(100L),
                "owner@demo.com",
                "AAPL",
                OrderSide.SELL,
                new Quantity(new BigDecimal("2")),
                new Money(new BigDecimal("130")),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                null
        );

        given(authentication.getName()).willReturn("intruder@demo.com");
        given(getOrdersUseCase.getById(new OrderId(100L))).willReturn(foreignOrder);

        assertThatThrownBy(() -> controller.execute(100L, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("another user's order");

        verify(getOrdersUseCase).getById(new OrderId(100L));
        verifyNoInteractions(executeOrderUseCase);
    }
}

