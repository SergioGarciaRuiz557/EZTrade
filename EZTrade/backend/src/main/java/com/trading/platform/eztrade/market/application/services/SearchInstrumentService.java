package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.in.SearchInstrumentUserCase;
import com.trading.platform.eztrade.market.application.ports.out.SearchInstrumentProviderPort;
import com.trading.platform.eztrade.market.domain.Instrument;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the {@link SearchInstrumentUserCase} use case.
 * <p>
 * By delegating the search to the {@link SearchInstrumentProviderPort} output
 * port, the application layer remains decoupled from the concrete technology
 * used to query the market (external API, database, etc.).
 * </p>
 */
@Service
public class SearchInstrumentService implements SearchInstrumentUserCase {
    private final SearchInstrumentProviderPort searchInstrumentProviderPort;

    public SearchInstrumentService(SearchInstrumentProviderPort searchInstrumentProviderPort) {
        this.searchInstrumentProviderPort = searchInstrumentProviderPort;
    }


    @Override
    public List<Instrument> searchInstruments(String input) {
        return searchInstrumentProviderPort.searchInstruments(input);
    }
}
