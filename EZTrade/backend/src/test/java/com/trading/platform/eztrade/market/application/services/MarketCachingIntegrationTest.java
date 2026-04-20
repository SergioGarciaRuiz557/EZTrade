package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.in.GetOverviewUserCase;
import com.trading.platform.eztrade.market.application.ports.in.GetPriceUserCase;
import com.trading.platform.eztrade.market.application.ports.out.GetOverviewProviderPort;
import com.trading.platform.eztrade.market.application.ports.out.GetPriceMarketProviderPort;
import com.trading.platform.eztrade.market.domain.InstrumentOverview;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig
@Import({MarketCacheConfig.class, GetPriceService.class, GetOverviewService.class, MarketCachingIntegrationTest.TestConfig.class})
class MarketCachingIntegrationTest {

    @Autowired
    private GetPriceUserCase getPriceService;

    @Autowired
    private GetOverviewUserCase getOverviewService;

    @Autowired
    private GetPriceMarketProviderPort getPriceMarketProviderPort;

    @Autowired
    private GetOverviewProviderPort getOverviewProviderPort;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(getPriceMarketProviderPort, getOverviewProviderPort);
    }

    @Test
    @DisplayName("getPrice usa cache para el mismo simbolo dentro del TTL")
    void getPrice_uses_cache_for_same_symbol() {
        Symbol symbol = new Symbol("IBM");
        MarketPrice expected = new MarketPrice(symbol, 150.5, Instant.now());
        given(getPriceMarketProviderPort.getMarketPrice(symbol)).willReturn(expected);

        MarketPrice first = getPriceService.getPrice(symbol);
        MarketPrice second = getPriceService.getPrice(symbol);

        assertThat(first).isEqualTo(expected);
        assertThat(second).isEqualTo(expected);
        verify(getPriceMarketProviderPort, times(1)).getMarketPrice(symbol);
    }

    @Test
    @DisplayName("getOverview usa cache para el mismo simbolo dentro del TTL")
    void getOverview_uses_cache_for_same_symbol() {
        Symbol symbol = new Symbol("AAPL");
        InstrumentOverview expected = new InstrumentOverview(
                "AAPL",
                "Apple Inc.",
                "Technology",
                "Consumer Electronics",
                2_500_000_000_000L,
                28.2
        );
        given(getOverviewProviderPort.getOverview(symbol)).willReturn(expected);

        InstrumentOverview first = getOverviewService.getOverview(symbol);
        InstrumentOverview second = getOverviewService.getOverview(symbol);

        assertThat(first).isEqualTo(expected);
        assertThat(second).isEqualTo(expected);
        verify(getOverviewProviderPort, times(1)).getOverview(symbol);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        GetPriceMarketProviderPort getPriceMarketProviderPort() {
            return Mockito.mock(GetPriceMarketProviderPort.class);
        }

        @Bean
        GetOverviewProviderPort getOverviewProviderPort() {
            return Mockito.mock(GetOverviewProviderPort.class);
        }
    }
}

