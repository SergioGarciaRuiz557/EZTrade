package com.trading.platform.eztrade.portfolio.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositionTest {

    @Test
    @DisplayName("increase recalcula el coste medio")
    void increase_recalculates_average_cost() {
        Position initial = Position.open("user@demo.com", "IBM", new BigDecimal("2"), new BigDecimal("100"));

        Position increased = initial.increase(new BigDecimal("1"), new BigDecimal("130"));

        assertThat(increased.quantity()).isEqualByComparingTo("3");
        assertThat(increased.averageCost()).isEqualByComparingTo("110.00000000");
    }

    @Test
    @DisplayName("reduce calcula PnL realizado y puede cerrar posicion")
    void reduce_computes_realized_pnl_and_can_close_position() {
        Position initial = Position.open("user@demo.com", "IBM", new BigDecimal("2"), new BigDecimal("100"));

        Position.SellResult partial = initial.reduce(new BigDecimal("1"), new BigDecimal("120"));
        assertThat(partial.realizedPnlDelta()).isEqualByComparingTo("20");
        assertThat(partial.position().quantity()).isEqualByComparingTo("1");

        Position.SellResult close = partial.position().reduce(new BigDecimal("1"), new BigDecimal("90"));
        assertThat(close.realizedPnlDelta()).isEqualByComparingTo("-10");
        assertThat(close.position().isClosed()).isTrue();
        assertThat(close.position().realizedPnl()).isEqualByComparingTo("10");
    }

    @Test
    @DisplayName("reduce falla cuando se intenta vender mas de lo disponible")
    void reduce_fails_if_selling_more_than_available() {
        Position initial = Position.open("user@demo.com", "IBM", new BigDecimal("1"), new BigDecimal("100"));

        assertThatThrownBy(() -> initial.reduce(new BigDecimal("2"), new BigDecimal("100")))
                .isInstanceOf(PortfolioDomainException.class)
                .hasMessageContaining("Cannot sell more");
    }
}

