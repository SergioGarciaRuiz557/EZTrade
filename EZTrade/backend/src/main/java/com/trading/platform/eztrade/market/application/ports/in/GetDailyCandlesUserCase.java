package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.Candle;
import com.trading.platform.eztrade.market.domain.Symbol;

import java.util.List;

/**
 * Use case for obtaining a symbol's daily candle series (OHLCV).
 */
public interface GetDailyCandlesUserCase {
    List<Candle> getDailyCandles(Symbol symbol);
}
