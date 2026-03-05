package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;

/**
 * Puerto de salida que define cómo se obtiene el precio de mercado de un símbolo
 * desde una fuente externa (por ejemplo, un proveedor de datos de mercado).
 */
public interface GetPriceMarketProviderPort {

    MarketPrice getMarketPrice(Symbol symbol);
}

