package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.Instrument;

import java.util.List;

/**
 * Puerto de salida para gestionar instrumentos financieros.
 */
public interface InstrumentRepositoryPort {

    List<Instrument> findAll();
}

