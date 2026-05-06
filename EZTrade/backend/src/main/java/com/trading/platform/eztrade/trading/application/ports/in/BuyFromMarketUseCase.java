package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.math.BigDecimal;

/**
 * Caso de uso para comprar acciones directamente al mercado.
 * <p>
 * La implementacion no recibe precio desde el cliente: lo obtiene desde market
 * para ejecutar la compra al precio actual de AlphaVantage.
 */
public interface BuyFromMarketUseCase {

    /**
     * Crea y ejecuta una orden BUY al precio actual de mercado.
     *
     * @param command usuario, simbolo y cantidad a comprar
     * @return orden de compra ya ejecutada
     */
    TradeOrder buy(BuyFromMarketCommand command);

    /**
     * Datos minimos para comprar al mercado.
     *
     * @param owner usuario comprador
     * @param symbol ticker de la accion
     * @param quantity numero de acciones a comprar
     */
    record BuyFromMarketCommand(String owner, String symbol, BigDecimal quantity) {
    }
}
