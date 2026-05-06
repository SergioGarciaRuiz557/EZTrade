package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.TradeOrder;

/**
 * Caso de uso para comprar una oferta de venta publicada por otro usuario.
 * <p>
 * Este flujo genera dos ordenes ejecutadas: una BUY para el comprador y la
 * propia SELL para el vendedor. Wallet y portfolio se actualizan despues por
 * los eventos ya existentes de ejecucion de orden.
 */
public interface BuySellOfferUseCase {

    /**
     * Compra una oferta SELL pendiente.
     *
     * @param command comprador e identificador de la oferta
     * @return resultado con la orden del comprador y la del vendedor
     */
    MarketplaceTradeResult buyOffer(BuySellOfferCommand command);

    /**
     * Datos necesarios para comprar una oferta.
     *
     * @param buyer usuario que compra la oferta
     * @param sellOfferId id de la orden SELL pendiente que se quiere comprar
     */
    record BuySellOfferCommand(String buyer, Long sellOfferId) {
    }

    /**
     * Resultado de una compraventa entre usuarios.
     *
     * @param buyerOrder orden BUY creada y ejecutada para el comprador
     * @param sellerOrder orden SELL ejecutada para el vendedor
     */
    record MarketplaceTradeResult(TradeOrder buyerOrder, TradeOrder sellerOrder) {
    }
}
