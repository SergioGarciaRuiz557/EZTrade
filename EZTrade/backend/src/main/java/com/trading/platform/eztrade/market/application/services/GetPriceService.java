package com.trading.platform.eztrade.market.application.services;

import com.trading.platform.eztrade.market.application.ports.in.GetPriceUserCase;
import com.trading.platform.eztrade.market.application.ports.out.GetPriceMarketProviderPort;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.springframework.stereotype.Service;

@Service
public class GetPriceService implements GetPriceUserCase {
    private final GetPriceMarketProviderPort getPriceMarketProviderPort;

    public GetPriceService(GetPriceMarketProviderPort getPriceMarketProviderPort) {
        this.getPriceMarketProviderPort = getPriceMarketProviderPort;
    }

    @Override
    public MarketPrice getPrice(Symbol symbol) {
        return getPriceMarketProviderPort.getMarketPrice(symbol);
    }
}
