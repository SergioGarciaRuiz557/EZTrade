package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.in.UpdateMarketPriceUseCase;
import com.trading.platform.eztrade.market.application.ports.out.MarketPriceEventPublisherPort;
import com.trading.platform.eztrade.market.application.ports.out.MarketPriceRepositoryPort;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.event.MarketPriceUpdatedEvent;
import org.springframework.stereotype.Service;

/**
 * Servicio para actualizar precios de mercado y publicar eventos.
 */
@Service
public class MarketPriceUpdateService implements UpdateMarketPriceUseCase {

    private final MarketPriceRepositoryPort marketPriceRepositoryPort;
    private final MarketPriceEventPublisherPort eventPublisherPort;
    private final MarketDataSubscriptionService subscriptionService;

    public MarketPriceUpdateService(MarketPriceRepositoryPort marketPriceRepositoryPort,
                                    MarketPriceEventPublisherPort eventPublisherPort,
                                    MarketDataSubscriptionService subscriptionService) {
        this.marketPriceRepositoryPort = marketPriceRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public void updateMarketPrice(MarketPrice price) {
        marketPriceRepositoryPort.save(price);
        MarketPriceUpdatedEvent event = new MarketPriceUpdatedEvent(price);
        eventPublisherPort.publish(event);
        subscriptionService.notifySubscribers(price);
    }
}

