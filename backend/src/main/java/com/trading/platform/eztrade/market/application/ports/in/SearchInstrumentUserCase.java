package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.Instrument;

import java.util.List;

/**
 * Application-layer use case for searching market instruments from user-entered text.
 */
public interface SearchInstrumentUserCase {
    List<Instrument> searchInstruments(String input);
}
