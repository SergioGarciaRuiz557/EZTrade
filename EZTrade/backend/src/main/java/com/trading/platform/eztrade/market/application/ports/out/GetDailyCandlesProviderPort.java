package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.Candle;
import com.trading.platform.eztrade.market.domain.Symbol;

import java.util.List;

/**
 * Output port that abstracts the origin of daily candle data (for example, an
 * external API or a database).
 */
public interface GetDailyCandlesProviderPort {
    List<Candle> getDailyCandles(Symbol symbol);
}
