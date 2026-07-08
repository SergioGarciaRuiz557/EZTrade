package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;

/**
 * Output port that defines how a symbol's market price is obtained from an
 * external source (for example, a market data provider).
 */
public interface GetPriceMarketProviderPort {

    MarketPrice getMarketPrice(Symbol symbol);
}

