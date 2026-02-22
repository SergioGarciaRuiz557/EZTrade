package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.MarketPrice;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para almacenar y consultar precios de mercado.
 */
public interface MarketPriceRepositoryPort {

    void save(MarketPrice price);

    Optional<MarketPrice> findByInstrument(String instrument);

    List<MarketPrice> findAll();
}

