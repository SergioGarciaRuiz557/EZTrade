package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.MarketPrice;

import java.util.List;

/**
 * Puerto de salida para proveedores externos de datos de mercado (APIs).
 */
public interface ExternalMarketDataProviderPort {

    List<MarketPrice> fetchCurrentPrices();
}

