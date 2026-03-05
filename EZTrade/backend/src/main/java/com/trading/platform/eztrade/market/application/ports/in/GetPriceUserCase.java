package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;

/**
 * Caso de uso para obtener el precio de mercado actual de un símbolo concreto.
 */
public interface GetPriceUserCase {

    MarketPrice getPrice(Symbol symbol);
}
