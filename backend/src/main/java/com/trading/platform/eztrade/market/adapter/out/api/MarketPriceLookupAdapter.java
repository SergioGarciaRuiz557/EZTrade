package com.trading.platform.eztrade.market.adapter.out.api;

import com.trading.platform.eztrade.market.api.MarketPriceLookupPort;
import com.trading.platform.eztrade.market.application.ports.in.GetPriceUserCase;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adapter that implements market's public price API.
 * <p>
 * Its role is to translate the simple call made by other modules
 * ({@link MarketPriceLookupPort#currentPrice(String)}) into market's internal
 * use case ({@link GetPriceUserCase}). This allows trading to validate prices
 * against Alpha Vantage without coupling to controllers or external providers.
 */
@Component
class MarketPriceLookupAdapter implements MarketPriceLookupPort {

    private final GetPriceUserCase getPriceUserCase;

    MarketPriceLookupAdapter(GetPriceUserCase getPriceUserCase) {
        this.getPriceUserCase = getPriceUserCase;
    }

    @Override
    public BigDecimal currentPrice(String symbol) {
        // Reuse the Symbol value object to keep the same normalization and
        // validation rules as the rest of the market module.
        MarketPrice marketPrice = getPriceUserCase.getPrice(Symbol.of(symbol));
        return BigDecimal.valueOf(marketPrice.price());
    }
}
