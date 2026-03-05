package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.in.GetOverviewUserCase;
import com.trading.platform.eztrade.market.application.ports.out.GetOverviewProviderPort;
import com.trading.platform.eztrade.market.domain.InstrumentOverview;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.springframework.stereotype.Service;

/**
 * Implementación del caso de uso {@link GetOverviewUserCase}.
 * <p>
 * Delegando en el puerto de salida {@link GetOverviewProviderPort},
 * permite obtener la información fundamental de un instrumento sin
 * acoplar la capa de aplicación a un proveedor concreto (por ejemplo,
 * Alpha Vantage).
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
