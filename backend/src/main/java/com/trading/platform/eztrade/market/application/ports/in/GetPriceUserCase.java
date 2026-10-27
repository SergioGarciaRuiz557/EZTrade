package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;

/**
 * Use case for obtaining the current market price of a specific symbol.
 */
public interface GetPriceUserCase {

    MarketPrice getPrice(Symbol symbol);
}
