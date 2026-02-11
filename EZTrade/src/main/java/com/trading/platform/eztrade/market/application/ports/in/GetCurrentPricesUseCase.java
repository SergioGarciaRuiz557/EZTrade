package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.MarketPrice;

import java.util.List;

/**
 * Caso de uso para obtener precios actuales de los instrumentos.
 */
public interface GetCurrentPricesUseCase {

    List<MarketPrice> getCurrentPrices();
}

