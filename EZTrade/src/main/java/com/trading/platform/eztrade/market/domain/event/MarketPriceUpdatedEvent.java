package com.trading.platform.eztrade.market.domain.event;

import com.trading.platform.eztrade.market.domain.MarketPrice;

/**
 * Evento de dominio que se dispara cuando se actualiza el precio de mercado de un instrumento.
 */
public class MarketPriceUpdatedEvent {

    private final MarketPrice marketPrice;

    public MarketPriceUpdatedEvent(MarketPrice marketPrice) {
        this.marketPrice = marketPrice;
    }

    public MarketPrice getMarketPrice() {
        return marketPrice;
    }
}

