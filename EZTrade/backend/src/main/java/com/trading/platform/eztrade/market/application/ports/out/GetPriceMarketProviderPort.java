package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;

import java.util.List;

/**
 * Puerto de salida para gestionar instrumentos financieros.
 */
public interface GetPriceMarketProviderPort {

    MarketPrice getMarketPrice(Symbol symbol);
}

