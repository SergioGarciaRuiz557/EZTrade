package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.MarketPrice;

/**
 * Caso de uso interno para actualizar el precio de mercado.
 */
public interface UpdateMarketPriceUseCase {

    void updateMarketPrice(MarketPrice price);
}

