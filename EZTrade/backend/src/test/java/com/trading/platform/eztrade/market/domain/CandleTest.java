package com.trading.platform.eztrade.market.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CandleTest {

    @Test
    @DisplayName("Los componentes del record Candle devuelven los valores pasados al constructor")
    void components_return_constructor_values() {
        LocalDateTime time = LocalDateTime.of(2026, 3, 4, 0, 0);
        Candle candle = new Candle(time, 1.0, 2.0, 0.5, 1.5, 1000L);

        assertThat(candle.time()).isEqualTo(time);
        assertThat(candle.open()).isEqualTo(1.0);
        assertThat(candle.high()).isEqualTo(2.0);
        assertThat(candle.low()).isEqualTo(0.5);
        assertThat(candle.close()).isEqualTo(1.5);
        assertThat(candle.volume()).isEqualTo(1000L);
    }
}

