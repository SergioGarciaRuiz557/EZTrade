package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.event.MarketPriceUpdatedEvent;

/**
 * Puerto de salida para publicar eventos de dominio de precios de mercado.
 */
public interface MarketPriceEventPublisherPort {

    void publish(MarketPriceUpdatedEvent event);
}

