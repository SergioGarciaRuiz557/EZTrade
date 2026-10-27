package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.out.GetPriceMarketProviderPort;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetPriceServiceTest {

    @Mock
    private GetPriceMarketProviderPort getPriceMarketProviderPort;

    @InjectMocks
    private GetPriceService getPriceService;

    @Test
    @DisplayName("getPrice delega en el proveedor de mercado y devuelve el precio obtenido")
    void get_price_delegates_to_provider_and_returns_market_price() {
        Symbol symbol = new Symbol("IBM");
        MarketPrice expectedPrice = new MarketPrice(symbol, 150.5, Instant.now());
        given(getPriceMarketProviderPort.getMarketPrice(symbol)).willReturn(expectedPrice);

        MarketPrice result = getPriceService.getPrice(symbol);

        assertThat(result).isEqualTo(expectedPrice);
        verify(getPriceMarketProviderPort).getMarketPrice(symbol);
    }
}

