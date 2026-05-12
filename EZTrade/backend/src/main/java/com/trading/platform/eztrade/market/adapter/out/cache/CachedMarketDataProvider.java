package com.trading.platform.eztrade.market.adapter.out.cache;

import com.trading.platform.eztrade.market.application.ports.out.GetDailyCandlesProviderPort;
import com.trading.platform.eztrade.market.application.ports.out.GetOverviewProviderPort;
import com.trading.platform.eztrade.market.application.ports.out.GetPriceMarketProviderPort;
import com.trading.platform.eztrade.market.domain.Candle;
import com.trading.platform.eztrade.market.domain.InstrumentOverview;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Decorador de infraestructura que cachea consultas al proveedor externo.
 * <p>
 * La capa de aplicacion sigue hablando contra puertos puros; las anotaciones
 * de cache quedan confinadas al adaptador de salida.
 */
@Primary
@Component
public class CachedMarketDataProvider implements GetPriceMarketProviderPort,
        GetOverviewProviderPort,
        GetDailyCandlesProviderPort {

    private final GetPriceMarketProviderPort priceProvider;
    private final GetOverviewProviderPort overviewProvider;
    private final GetDailyCandlesProviderPort dailyCandlesProvider;

    public CachedMarketDataProvider(@Qualifier("marketDataProvider") GetPriceMarketProviderPort priceProvider,
                                    @Qualifier("marketDataProvider") GetOverviewProviderPort overviewProvider,
                                    @Qualifier("marketDataProvider") GetDailyCandlesProviderPort dailyCandlesProvider) {
        this.priceProvider = priceProvider;
        this.overviewProvider = overviewProvider;
        this.dailyCandlesProvider = dailyCandlesProvider;
    }

    @Override
    @Cacheable(cacheNames = "marketPrice", key = "#symbol.value()", unless = "#result == null")
    public MarketPrice getMarketPrice(Symbol symbol) {
        return priceProvider.getMarketPrice(symbol);
    }

    @Override
    @Cacheable(cacheNames = "instrumentOverview", key = "#symbol.value()", unless = "#result == null")
    public InstrumentOverview getOverview(Symbol symbol) {
        return overviewProvider.getOverview(symbol);
    }

    @Override
    @Cacheable(cacheNames = "dailyCandles", key = "#symbol.value()", unless = "#result == null")
    public List<Candle> getDailyCandles(Symbol symbol) {
        return dailyCandlesProvider.getDailyCandles(symbol);
    }
}
