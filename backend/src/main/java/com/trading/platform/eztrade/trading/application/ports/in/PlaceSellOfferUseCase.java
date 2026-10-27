package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.math.BigDecimal;

/**
 * Use case for publishing a sell offer backed by the user's own shares.
 * <p>
 * The implementation validates that the user has enough shares and that the
 * offered price is not higher than the current Alpha Vantage price.
 */
public interface PlaceSellOfferUseCase {

    /**
     * Registers a pending SELL order so other users can buy it from the marketplace.
     *
     * @param command selling user, symbol, quantity, and offered price
     * @return pending SELL order
     */
    TradeOrder placeSellOffer(PlaceSellOfferCommand command);

    /**
     * Data required to publish an offer.
     *
     * @param owner selling user
     * @param symbol stock ticker
     * @param quantity number of offered shares
     * @param price offered unit price, at most the market price
     */
    record PlaceSellOfferCommand(String owner, String symbol, BigDecimal quantity, BigDecimal price) {
    }
}
