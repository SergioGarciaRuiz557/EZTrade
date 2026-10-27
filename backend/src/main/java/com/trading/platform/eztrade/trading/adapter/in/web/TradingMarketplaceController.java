package com.trading.platform.eztrade.trading.adapter.in.web;

import com.trading.platform.eztrade.trading.adapter.in.web.dto.BuyFromMarketRequest;
import com.trading.platform.eztrade.trading.adapter.in.web.dto.MarketplaceTradeResponse;
import com.trading.platform.eztrade.trading.adapter.in.web.dto.PlaceSellOfferRequest;
import com.trading.platform.eztrade.trading.adapter.in.web.dto.TradeOrderResponse;
import com.trading.platform.eztrade.trading.application.ports.in.BuyFromMarketUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.BuySellOfferUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.GetSellOffersUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.PlaceSellOfferUseCase;
import com.trading.platform.eztrade.trading.domain.TradeOrder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the user-to-user stock marketplace.
 * <p>
 * It is separated from {@link TradingController} because these endpoints are
 * not just part of the generic order lifecycle: they model product-specific
 * flows such as buying from the market, publishing offers, and buying offers
 * from other users.
 */
@RestController
@RequestMapping("/api/v1/trading")
public class TradingMarketplaceController {

    private final BuyFromMarketUseCase buyFromMarketUseCase;
    private final PlaceSellOfferUseCase placeSellOfferUseCase;
    private final GetSellOffersUseCase getSellOffersUseCase;
    private final BuySellOfferUseCase buySellOfferUseCase;

    public TradingMarketplaceController(BuyFromMarketUseCase buyFromMarketUseCase,
                                        PlaceSellOfferUseCase placeSellOfferUseCase,
                                        GetSellOffersUseCase getSellOffersUseCase,
                                        BuySellOfferUseCase buySellOfferUseCase) {
        this.buyFromMarketUseCase = buyFromMarketUseCase;
        this.placeSellOfferUseCase = placeSellOfferUseCase;
        this.getSellOffersUseCase = getSellOffersUseCase;
        this.buySellOfferUseCase = buySellOfferUseCase;
    }

    /**
     * Buys shares directly from the market.
     * <p>
     * The user only sends symbol and quantity. The price is obtained inside the
     * use case from AlphaVantage, and the order is created as an executed BUY.
     */
    @PostMapping("/market/buy")
    public ResponseEntity<TradeOrderResponse> buyFromMarket(@Valid @RequestBody BuyFromMarketRequest request,
                                                            Authentication authentication) {
        TradeOrder order = buyFromMarketUseCase.buy(new BuyFromMarketUseCase.BuyFromMarketCommand(
                authentication.getName(),
                request.symbol(),
                request.quantity()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(TradeOrderResponse.from(order));
    }

    /**
     * Publishes a sell offer so other users can buy it.
     * <p>
     * The use case validates two main rules: the seller must have enough
     * shares, and the price cannot exceed the current market price.
     */
    @PostMapping("/offers")
    public ResponseEntity<TradeOrderResponse> placeSellOffer(@Valid @RequestBody PlaceSellOfferRequest request,
                                                             Authentication authentication) {
        TradeOrder offer = placeSellOfferUseCase.placeSellOffer(new PlaceSellOfferUseCase.PlaceSellOfferCommand(
                authentication.getName(),
                request.symbol(),
                request.quantity(),
                request.price()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(TradeOrderResponse.from(offer));
    }

    /**
     * Lists pending sell offers visible to the authenticated user.
     * <p>
     * It never returns the user's own offers. When a symbol is provided, it also
     * filters out offers whose price is already above the current market price.
     */
    @GetMapping("/offers")
    public ResponseEntity<List<TradeOrderResponse>> getAvailableSellOffers(@RequestParam(required = false) String symbol,
                                                                           Authentication authentication) {
        List<TradeOrderResponse> offers = getSellOffersUseCase
                .getAvailableSellOffers(authentication.getName(), symbol)
                .stream()
                .map(TradeOrderResponse::from)
                .toList();
        return ResponseEntity.ok(offers);
    }

    /**
     * Buys a SELL offer published by another user.
     * <p>
     * Two orders are executed internally: a BUY for the buyer and the original
     * SELL for the seller. Wallet and portfolio side effects still happen
     * through the existing execution events.
     */
    @PostMapping("/offers/{offerId}/buy")
    public ResponseEntity<MarketplaceTradeResponse> buyOffer(@PathVariable Long offerId,
                                                             Authentication authentication) {
        BuySellOfferUseCase.MarketplaceTradeResult result = buySellOfferUseCase.buyOffer(
                new BuySellOfferUseCase.BuySellOfferCommand(authentication.getName(), offerId)
        );
        return ResponseEntity.ok(MarketplaceTradeResponse.from(result));
    }
}
