package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.in.GetCurrentPricesUseCase;
import com.trading.platform.eztrade.market.application.ports.in.GetInstrumentsUseCase;
import com.trading.platform.eztrade.market.application.ports.out.InstrumentRepositoryPort;
import com.trading.platform.eztrade.market.application.ports.out.MarketPriceRepositoryPort;
import com.trading.platform.eztrade.market.domain.Instrument;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de consulta de instrumentos y precios de mercado.
 */
@Service
public class MarketQueryService implements GetInstrumentsUseCase, GetCurrentPricesUseCase {

    private final InstrumentRepositoryPort instrumentRepositoryPort;
    private final MarketPriceRepositoryPort marketPriceRepositoryPort;

    public MarketQueryService(InstrumentRepositoryPort instrumentRepositoryPort,
                              MarketPriceRepositoryPort marketPriceRepositoryPort) {
        this.instrumentRepositoryPort = instrumentRepositoryPort;
        this.marketPriceRepositoryPort = marketPriceRepositoryPort;
    }

    @Override
    public List<Instrument> getInstruments() {
        return instrumentRepositoryPort.findAll();
    }

    @Override
    public List<MarketPrice> getCurrentPrices() {
        return marketPriceRepositoryPort.findAll();
    }
}

