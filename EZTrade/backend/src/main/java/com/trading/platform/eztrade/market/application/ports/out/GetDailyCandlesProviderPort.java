package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.Candle;
import com.trading.platform.eztrade.market.domain.Symbol;

import java.util.List;

/**
 * Puerto de salida que abstrae el origen de los datos de velas diarias
 * (por ejemplo, una API externa o una base de datos).
 */
public interface GetDailyCandlesProviderPort {
    List<Candle> getDailyCandles(Symbol symbol);
}
