package com.trading.platform.eztrade.trading.adapter.in.web.dto;

import com.trading.platform.eztrade.trading.application.ports.in.BuySellOfferUseCase;

/**
 * Output DTO for a user-to-user purchase.
 * <p>
 * Returns the two orders that represent the complete operation: the buyer's BUY
 * and the seller's SELL.
 *
 * @param buyerOrder order created and executed for the buyer
 * @param sellerOrder SELL offer executed for the seller
 */
public record MarketplaceTradeResponse(
        TradeOrderResponse buyerOrder,
        TradeOrderResponse sellerOrder
) {

    /**
     * Converts the use case result into a REST response.
     *
     * @param result domain/application result of the trade
     * @return client-serializable DTO
     */
    public static MarketplaceTradeResponse from(BuySellOfferUseCase.MarketplaceTradeResult result) {
        return new MarketplaceTradeResponse(
                TradeOrderResponse.from(result.buyerOrder()),
                TradeOrderResponse.from(result.sellerOrder())
        );
    }
}
