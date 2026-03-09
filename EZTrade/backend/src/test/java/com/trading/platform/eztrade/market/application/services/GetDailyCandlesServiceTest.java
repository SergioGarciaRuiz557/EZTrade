package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.out.GetDailyCandlesProviderPort;
import com.trading.platform.eztrade.market.domain.Candle;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetDailyCandlesServiceTest {

    @Mock
    private GetDailyCandlesProviderPort getDailyCandlesProviderPort;

    @InjectMocks
    private GetDailyCandlesService getDailyCandlesService;

    @Test
    @DisplayName("getDailyCandles delega en el proveedor y devuelve la lista de velas")
    void get_daily_candles_delegates_to_provider_and_returns_candles() {
        Symbol symbol = new Symbol("IBM");
        List<Candle> candles = List.of(
                new Candle(LocalDateTime.of(2026, 3, 4, 0, 0), 1.0, 2.0, 0.5, 1.5, 1000L)
        );
        given(getDailyCandlesProviderPort.getDailyCandles(symbol)).willReturn(candles);

        List<Candle> result = getDailyCandlesService.getDailyCandles(symbol);

        assertThat(result).containsExactlyElementsOf(candles);
        verify(getDailyCandlesProviderPort).getDailyCandles(symbol);
    }
}

