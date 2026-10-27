package com.trading.platform.eztrade.market.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InstrumentTest {

    @Test
    @DisplayName("Los componentes del record Instrument devuelven los valores pasados al constructor")
    void components_return_constructor_values() {
        Instrument instrument = new Instrument("IBM", "International Business Machines", "United States", "USD");

        assertThat(instrument.symbol()).isEqualTo("IBM");
        assertThat(instrument.name()).isEqualTo("International Business Machines");
        assertThat(instrument.region()).isEqualTo("United States");
        assertThat(instrument.currency()).isEqualTo("USD");
    }
}

