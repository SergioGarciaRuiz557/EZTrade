package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.out.GetOverviewProviderPort;
import com.trading.platform.eztrade.market.domain.InstrumentOverview;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetOverviewServiceTest {

    @Mock
    private GetOverviewProviderPort getOverviewProviderPort;

    @InjectMocks
    private GetOverviewService getOverviewService;

    @Test
    @DisplayName("getOverview delega en el proveedor y devuelve el overview obtenido")
    void get_overview_delegates_to_provider_and_returns_overview() {
        Symbol symbol = new Symbol("IBM");
        InstrumentOverview overview = new InstrumentOverview(
                "IBM",
                "International Business Machines",
                "Technology",
                "Information Technology Services",
                1_000_000_000L,
                15.5
        );
        given(getOverviewProviderPort.getOverview(symbol)).willReturn(overview);

        InstrumentOverview result = getOverviewService.getOverview(symbol);

        assertThat(result).isEqualTo(overview);
        verify(getOverviewProviderPort).getOverview(symbol);
    }
}

