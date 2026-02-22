package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.Instrument;

import java.util.List;

/**
 * Caso de uso para obtener todos los instrumentos disponibles.
 */
public interface GetInstrumentsUseCase {

    List<Instrument> getInstruments();
}

