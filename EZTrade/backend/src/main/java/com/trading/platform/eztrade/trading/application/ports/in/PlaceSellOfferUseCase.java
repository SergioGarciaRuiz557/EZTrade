package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.math.BigDecimal;

/**
 * Caso de uso para publicar una oferta de venta respaldada por acciones propias.
 * <p>
 * La implementacion valida que el usuario tenga acciones suficientes y que el
 * precio ofertado no sea superior al precio actual de AlphaVantage.
 */
public interface PlaceSellOfferUseCase {

    /**
     * Registra una orden SELL en estado pendiente para que otros usuarios puedan
     * comprarla desde el marketplace.
     *
     * @param command usuario vendedor, simbolo, cantidad y precio ofertado
     * @return orden SELL pendiente
     */
    TradeOrder placeSellOffer(PlaceSellOfferCommand command);

    /**
     * Datos necesarios para publicar una oferta.
     *
     * @param owner usuario vendedor
     * @param symbol ticker de la accion
     * @param quantity numero de acciones ofrecidas
     * @param price precio unitario ofertado, como maximo el precio de mercado
     */
    record PlaceSellOfferCommand(String owner, String symbol, BigDecimal quantity, BigDecimal price) {
    }
}
