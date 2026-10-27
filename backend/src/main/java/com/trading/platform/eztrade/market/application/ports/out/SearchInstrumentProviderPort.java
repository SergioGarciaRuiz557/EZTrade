package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.Instrument;

import java.util.List;

/**
 * Output port that defines how the application accesses an external source to
 * search market instruments.
 */
public interface SearchInstrumentProviderPort {
    List<Instrument> searchInstruments(String input);
}
