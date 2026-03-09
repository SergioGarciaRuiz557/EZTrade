package com.trading.platform.eztrade.market.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MarketPriceTest {

    @Test
    @DisplayName("Getters del record MarketPrice devuelven los valores pasados al constructor")
    void getters_return_constructor_values() {
        Instant now = Instant.now();
        Symbol symbol = new Symbol("AAPL");
        MarketPrice marketPrice = new MarketPrice(symbol, 10.0, now);

        assertThat(marketPrice.symbol()).isEqualTo(symbol);
        assertThat(marketPrice.price()).isEqualTo(10.0);
        assertThat(marketPrice.timestamp()).isEqualTo(now);
    }
}

