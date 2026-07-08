package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.math.BigDecimal;

/**
 * Use case for buying shares directly from the market.
 * <p>
 * The implementation does not receive the price from the client: it obtains it
 * from market to execute the purchase at the current Alpha Vantage price.
 */
public interface BuyFromMarketUseCase {

    /**
     * Creates and executes a BUY order at the current market price.
     *
     * @param command user, symbol, and quantity to buy
     * @return already executed buy order
     */
    TradeOrder buy(BuyFromMarketCommand command);

    /**
     * Minimum data required to buy from the market.
     *
     * @param owner buying user
     * @param symbol stock ticker
     * @param quantity number of shares to buy
     */
    record BuyFromMarketCommand(String owner, String symbol, BigDecimal quantity) {
    }
}
