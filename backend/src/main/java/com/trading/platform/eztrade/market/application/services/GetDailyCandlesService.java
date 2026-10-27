package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.in.GetDailyCandlesUserCase;
import com.trading.platform.eztrade.market.application.ports.out.GetDailyCandlesProviderPort;
import com.trading.platform.eztrade.market.domain.Candle;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the {@link GetDailyCandlesUserCase} use case.
 * <p>
 * Encapsulates orchestration logic to obtain a symbol's daily candle series,
 * delegating data-source access to the {@link GetDailyCandlesProviderPort}
 * output port.
 */
@Service
public class GetDailyCandlesService implements GetDailyCandlesUserCase {
    private final GetDailyCandlesProviderPort getDailyCandlesProviderPort;

    public GetDailyCandlesService(GetDailyCandlesProviderPort getDailyCandlesProviderPort) {
        this.getDailyCandlesProviderPort = getDailyCandlesProviderPort;
    }

    @Override
    public List<Candle> getDailyCandles(Symbol symbol) {
        return getDailyCandlesProviderPort.getDailyCandles(symbol);
    }
}
