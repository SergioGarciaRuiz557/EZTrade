package com.trading.platform.eztrade.market.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InstrumentOverviewTest {

    @Test
    @DisplayName("Los componentes del record InstrumentOverview devuelven los valores pasados al constructor")
    void components_return_constructor_values() {
        InstrumentOverview overview = new InstrumentOverview(
                "IBM",
                "International Business Machines",
                "Technology",
                "Information Technology Services",
                1_000_000_000L,
                15.5
        );

        assertThat(overview.symbol()).isEqualTo("IBM");
        assertThat(overview.name()).isEqualTo("International Business Machines");
        assertThat(overview.sector()).isEqualTo("Technology");
        assertThat(overview.industry()).isEqualTo("Information Technology Services");
        assertThat(overview.marketCap()).isEqualTo(1_000_000_000L);
        assertThat(overview.peRatio()).isEqualTo(15.5);
    }
}

