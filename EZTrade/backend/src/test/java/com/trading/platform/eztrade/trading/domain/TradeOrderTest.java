package com.trading.platform.eztrade.trading.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeOrderTest {

    @Test
    @DisplayName("place crea una orden pendiente con simbolo en mayusculas")
    void place_creates_pending_order() {
        TradeOrder order = TradeOrder.place("user@demo.com", "ibm", OrderSide.BUY,
                new Quantity(new BigDecimal("2")), new Money(new BigDecimal("10")));

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.symbol()).isEqualTo("IBM");
        assertThat(order.totalAmount().value()).isEqualByComparingTo("20");
    }

    @Test
    @DisplayName("execute cambia el estado de PENDING a EXECUTED")
    void execute_changes_status() {
        TradeOrder order = TradeOrder.place("user@demo.com", "IBM", OrderSide.BUY,
                new Quantity(new BigDecimal("1")), new Money(new BigDecimal("5")));

        TradeOrder executed = order.execute();

        assertThat(executed.status()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(executed.executedAt()).isNotNull();
    }

    @Test
    @DisplayName("cancel falla cuando quien cancela no es el propietario")
    void cancel_fails_for_non_owner() {
        TradeOrder order = TradeOrder.place("owner@demo.com", "IBM", OrderSide.SELL,
                new Quantity(new BigDecimal("1")), new Money(new BigDecimal("5")));

        assertThatThrownBy(() -> order.cancel("other@demo.com"))
                .isInstanceOf(TradingDomainException.class)
                .hasMessageContaining("Only the owner");
    }
}

