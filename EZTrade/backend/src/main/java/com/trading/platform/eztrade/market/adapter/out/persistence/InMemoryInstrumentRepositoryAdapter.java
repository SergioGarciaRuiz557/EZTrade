package com.trading.platform.eztrade.market.adapter.out.persistence;

import com.trading.platform.eztrade.market.application.ports.out.InstrumentRepositoryPort;
import com.trading.platform.eztrade.market.domain.Instrument;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementacin en memoria del repositorio de instrumentos.
 */
@Repository
public class InMemoryInstrumentRepositoryAdapter implements InstrumentRepositoryPort {

    private final List<Instrument> instruments = new ArrayList<>();

    @PostConstruct
    public void init() {
        instruments.add(new Instrument("AAPL", "Apple Inc.", "NASDAQ"));
        instruments.add(new Instrument("MSFT", "Microsoft Corp.", "NASDAQ"));
        instruments.add(new Instrument("GOOGL", "Alphabet Inc.", "NASDAQ"));
    }

    @Override
    public List<Instrument> findAll() {
        return Collections.unmodifiableList(instruments);
    }
}

