package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

/**
 * Puerto de entrada para ejecutar una orden existente.
 */
public interface ExecuteOrderUseCase {

    /**
     * Ejecuta una orden pendiente.
     *
     * @param orderId identificador de la orden
     * @return orden ejecutada
     */
    TradeOrder execute(OrderId orderId);
}
