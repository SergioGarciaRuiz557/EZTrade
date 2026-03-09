package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.in.SearchInstrumentUserCase;
import com.trading.platform.eztrade.market.application.ports.out.SearchInstrumentProviderPort;
import com.trading.platform.eztrade.market.domain.Instrument;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del caso de uso {@link SearchInstrumentUserCase}.
 * <p>
 * Delegando la búsqueda en el puerto de salida {@link SearchInstrumentProviderPort},
 * permite que la capa de aplicación permanezca desacoplada de la tecnología concreta
 * usada para consultar el mercado (API externa, base de datos, etc.).
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
