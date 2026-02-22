package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.in.SubscribeMarketDataUseCase;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Servicio en memoria para gestionar suscriptores de datos de mercado.
 */
@Service
public class MarketDataSubscriptionService implements SubscribeMarketDataUseCase {

    private final List<Consumer<MarketPrice>> subscribers = new CopyOnWriteArrayList<>();

    @Override
    public void subscribe(Consumer<MarketPrice> consumer) {
        subscribers.add(consumer);
    }

    public void notifySubscribers(MarketPrice price) {
        subscribers.forEach(c -> c.accept(price));
    }
}

