package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.MarketPrice;

import java.util.function.Consumer;

/**
 * Caso de uso para suscribirse a datos de mercado.
 */
public interface SubscribeMarketDataUseCase {

    void subscribe(Consumer<MarketPrice> consumer);
}

