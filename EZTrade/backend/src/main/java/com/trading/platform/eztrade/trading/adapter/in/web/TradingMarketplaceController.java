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
 * Controlador REST para el marketplace de acciones entre usuarios.
 * <p>
 * Se separa de {@link TradingController} porque estos endpoints no son solo el
 * ciclo de vida generico de ordenes: modelan flujos especificos de producto,
 * como comprar al mercado, publicar ofertas y comprar ofertas de otros usuarios.
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
     * Compra acciones directamente al mercado.
     * <p>
     * El usuario solo envia simbolo y cantidad. El precio se obtiene dentro del
     * caso de uso desde AlphaVantage y la orden se crea como BUY ejecutada.
     */
    @PostMapping("/market/buy")
    public ResponseEntity<TradeOrderResponse> buyFromMarket(@RequestBody BuyFromMarketRequest request,
                                                            Authentication authentication) {
        TradeOrder order = buyFromMarketUseCase.buy(new BuyFromMarketUseCase.BuyFromMarketCommand(
                authentication.getName(),
                request.symbol(),
                request.quantity()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(TradeOrderResponse.from(order));
    }

    /**
     * Publica una oferta de venta para que otros usuarios puedan comprarla.
     * <p>
     * El caso de uso valida dos reglas principales: el vendedor debe tener
     * acciones suficientes y el precio no puede superar el precio actual.
     */
    @PostMapping("/offers")
    public ResponseEntity<TradeOrderResponse> placeSellOffer(@RequestBody PlaceSellOfferRequest request,
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
     * Lista ofertas de venta pendientes visibles para el usuario autenticado.
     * <p>
     * Nunca devuelve ofertas del propio usuario. Si se envia simbolo, tambien
     * filtra ofertas cuyo precio ya haya quedado por encima del mercado actual.
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
     * Compra una oferta SELL publicada por otro usuario.
     * <p>
     * Internamente se ejecutan dos ordenes: una BUY para el comprador y la SELL
     * original para el vendedor. Los efectos en wallet y portfolio siguen
     * ocurriendo por los eventos de ejecucion ya existentes.
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
