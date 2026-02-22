package com.trading.platform.eztrade.market.adapter.out.external;

import com.trading.platform.eztrade.market.application.ports.out.ExternalMarketDataProviderPort;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Ejemplo de adaptador para proveedores externos de datos de mercado.
 * De momento devuelve datos simulados; aqu se integrarn APIs reales.
 */
@Component
public class ExternalMarketDataProviderAdapter implements ExternalMarketDataProviderPort {

    @Override
    public List<MarketPrice> fetchCurrentPrices() {
        MarketPrice example = new MarketPrice("AAPL", BigDecimal.valueOf(183.45), Instant.now());
        return Collections.singletonList(example);
    }
}

