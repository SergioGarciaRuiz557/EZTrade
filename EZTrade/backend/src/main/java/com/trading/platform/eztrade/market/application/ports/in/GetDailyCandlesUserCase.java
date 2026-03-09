package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.Candle;
import com.trading.platform.eztrade.market.domain.Symbol;

import java.util.List;

/**
 * Caso de uso para obtener la serie de velas diarias (OHLCV) de un símbolo.
 */
public interface GetDailyCandlesUserCase {
    List<Candle> getDailyCandles(Symbol symbol);
}
