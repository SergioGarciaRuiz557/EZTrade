package com.trading.platform.eztrade.trading.adapter.in.web.dto;

import com.trading.platform.eztrade.trading.application.ports.in.BuySellOfferUseCase;

/**
 * DTO de salida para una compra entre usuarios.
 * <p>
 * Devuelve las dos ordenes que representan la operacion completa:
 * la BUY del comprador y la SELL del vendedor.
 *
 * @param buyerOrder orden creada y ejecutada para el comprador
 * @param sellerOrder oferta SELL ejecutada para el vendedor
 */
public record MarketplaceTradeResponse(
        TradeOrderResponse buyerOrder,
        TradeOrderResponse sellerOrder
) {

    /**
     * Convierte el resultado del caso de uso a una respuesta REST.
     *
     * @param result resultado de dominio/aplicacion de la compraventa
     * @return DTO serializable para el cliente
     */
    public static MarketplaceTradeResponse from(BuySellOfferUseCase.MarketplaceTradeResult result) {
        return new MarketplaceTradeResponse(
                TradeOrderResponse.from(result.buyerOrder()),
                TradeOrderResponse.from(result.sellerOrder())
        );
    }
}
