package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.TradeOrder;

/**
 * Use case for buying a sell offer published by another user.
 * <p>
 * This flow generates two executed orders: a BUY for the buyer and the SELL
 * itself for the seller. Wallet and portfolio are updated later through the
 * existing order-execution events.
 */
public interface BuySellOfferUseCase {

    /**
     * Buys a pending SELL offer.
     *
     * @param command buyer and offer identifier
     * @return result with the buyer order and seller order
     */
    MarketplaceTradeResult buyOffer(BuySellOfferCommand command);

    /**
     * Data required to buy an offer.
     *
     * @param buyer user buying the offer
     * @param sellOfferId id of the pending SELL order to buy
     */
    record BuySellOfferCommand(String buyer, Long sellOfferId) {
    }

    /**
     * Result of a marketplace trade between users.
     *
     * @param buyerOrder BUY order created and executed for the buyer
     * @param sellerOrder SELL order executed for the seller
     */
    record MarketplaceTradeResult(TradeOrder buyerOrder, TradeOrder sellerOrder) {
    }
}
