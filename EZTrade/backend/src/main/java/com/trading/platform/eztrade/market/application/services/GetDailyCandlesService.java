package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.in.GetDailyCandlesUserCase;
import com.trading.platform.eztrade.market.application.ports.out.GetDailyCandlesProviderPort;
import com.trading.platform.eztrade.market.domain.Candle;
import com.trading.platform.eztrade.market.domain.Symbol;

import java.util.List;

/**
 * Implementación del caso de uso {@link GetDailyCandlesUserCase}.
 * <p>
 * Encapsula la lógica de orquestación para obtener la serie de velas diarias
 * de un símbolo, delegando el acceso a la fuente de datos en el puerto de
 * salida {@link GetDailyCandlesProviderPort}.
 */
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
