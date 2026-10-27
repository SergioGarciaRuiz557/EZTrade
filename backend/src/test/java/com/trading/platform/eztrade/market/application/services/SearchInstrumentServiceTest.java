package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.out.SearchInstrumentProviderPort;
import com.trading.platform.eztrade.market.domain.Instrument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchInstrumentServiceTest {

    @Mock
    private SearchInstrumentProviderPort searchInstrumentProviderPort;

    @InjectMocks
    private SearchInstrumentService searchInstrumentService;

    @Test
    @DisplayName("searchInstruments delega en el proveedor y devuelve la lista de instrumentos")
    void search_instruments_delegates_to_provider_and_returns_instruments() {
        String input = "IBM";
        List<Instrument> instruments = List.of(
                new Instrument("IBM", "International Business Machines", "United States", "USD")
        );
        given(searchInstrumentProviderPort.searchInstruments(input)).willReturn(instruments);

        List<Instrument> result = searchInstrumentService.searchInstruments(input);

        assertThat(result).containsExactlyElementsOf(instruments);
        verify(searchInstrumentProviderPort).searchInstruments(input);
    }
}

