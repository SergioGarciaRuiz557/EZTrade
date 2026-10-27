package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.util.List;

/**
 * Use case for listing pending SELL offers that a user can buy.
 * <p>
 * The list excludes the buyer's own offers to prevent self-purchase and, when
 * filtered by symbol, validates that the price does not exceed the current
 * market price.
 */
public interface GetSellOffersUseCase {

    /**
     * Gets available sell offers.
     *
     * @param buyer user querying the marketplace
     * @param symbol optional symbol used to filter offers
     * @return pending SELL orders available to that buyer
     */
    List<TradeOrder> getAvailableSellOffers(String buyer, String symbol);
}
