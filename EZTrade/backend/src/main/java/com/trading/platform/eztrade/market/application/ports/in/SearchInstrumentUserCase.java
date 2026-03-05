package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.Instrument;

import java.util.List;

/**
 * Caso de uso de la capa de aplicación para buscar instrumentos de mercado
 * a partir de un texto introducido por el usuario.
 */
public interface SearchInstrumentUserCase {
    List<Instrument> searchInstruments(String input);
}
