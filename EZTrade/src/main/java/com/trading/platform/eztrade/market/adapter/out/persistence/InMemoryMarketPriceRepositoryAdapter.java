package com.trading.platform.eztrade.market.adapter.out.persistence;

import com.trading.platform.eztrade.market.application.ports.out.MarketPriceRepositoryPort;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementacin en memoria del repositorio de precios de mercado.
 */
@Repository
public class InMemoryMarketPriceRepositoryAdapter implements MarketPriceRepositoryPort {

    private final Map<String, MarketPrice> prices = new ConcurrentHashMap<>();

    @Override
    public void save(MarketPrice price) {
        prices.put(price.getInstrument(), price);
    }

    @Override
    public Optional<MarketPrice> findByInstrument(String instrument) {
        return Optional.ofNullable(prices.get(instrument));
    }

    @Override
    public List<MarketPrice> findAll() {
        return new ArrayList<>(prices.values());
    }
}

