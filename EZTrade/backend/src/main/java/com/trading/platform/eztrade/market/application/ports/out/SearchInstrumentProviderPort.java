package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.Instrument;

import java.util.List;

/**
 * Puerto de salida que define cómo la aplicación accede a una fuente externa
 * para realizar búsquedas de instrumentos en el mercado.
 */
public interface SearchInstrumentProviderPort {
    List<Instrument> searchInstruments(String input);
}
