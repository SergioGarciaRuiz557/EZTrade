package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.in.GetOverviewUserCase;
import com.trading.platform.eztrade.market.application.ports.out.GetOverviewProviderPort;
import com.trading.platform.eztrade.market.domain.InstrumentOverview;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link GetOverviewUserCase} use case.
 * <p>
 * By delegating to the {@link GetOverviewProviderPort} output port, it obtains
 * an instrument's fundamental information without coupling the application layer
 * to a concrete provider (for example, Alpha Vantage).
 */
@Service
public class GetOverviewService implements GetOverviewUserCase {
    private final GetOverviewProviderPort getOverviewProviderPort;

    public GetOverviewService(GetOverviewProviderPort getOverviewProviderPort) {
        this.getOverviewProviderPort = getOverviewProviderPort;
    }

    @Override
    public InstrumentOverview getOverview(Symbol symbol) {
        return getOverviewProviderPort.getOverview(symbol);
    }
}
