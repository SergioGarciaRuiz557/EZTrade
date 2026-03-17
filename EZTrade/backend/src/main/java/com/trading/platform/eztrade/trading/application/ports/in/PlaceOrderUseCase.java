package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.OrderSide;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.math.BigDecimal;

/**
 * Puerto de entrada para registrar una nueva orden de trading.
 * <p>
 * Define el contrato que consumen los adaptadores de entrada (REST, mensajeria, etc.).
 */
public interface PlaceOrderUseCase {

    /**
     * Crea una orden pendiente en el dominio.
     *
     * @param command datos de entrada para la orden
     * @return orden creada y persistida
     */
    TradeOrder place(PlaceOrderCommand command);

    /**
     * Comando inmutable con los datos necesarios para crear una orden.
     *
     * @param owner propietario de la orden
     * @param symbol simbolo del activo
     * @param side tipo de orden
     * @param quantity cantidad solicitada
     * @param price precio unitario
     */
    record PlaceOrderCommand(
            String owner,
            String symbol,
            OrderSide side,
            BigDecimal quantity,
            BigDecimal price
    ) {
    }
}
