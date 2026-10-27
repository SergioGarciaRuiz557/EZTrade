package com.trading.platform.eztrade.market.api;

import org.springframework.modulith.NamedInterface;

import java.math.BigDecimal;

/**
 * Public API of the market module so other modules can query a symbol's current
 * price without depending on market's internal services.
 * <p>
 * Marked as {@link NamedInterface} because Spring Modulith only allows other
 * modules to depend on explicitly declared public interfaces.
 */
@NamedInterface
public interface MarketPriceLookupPort {

    /**
     * Returns the current market price for the given symbol.
     * <p>
     * In the real implementation, this price comes from the existing market
     * flow, which queries Alpha Vantage and applies its cache.
     *
     * @param symbol normalized or normalizable ticker, for example AAPL
     * @return current price as {@link BigDecimal} for monetary rules
     */
    BigDecimal currentPrice(String symbol);
}
