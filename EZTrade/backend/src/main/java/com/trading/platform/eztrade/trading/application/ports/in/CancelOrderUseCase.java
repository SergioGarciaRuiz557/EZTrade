package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

/**
 * Puerto de entrada para cancelar una orden.
 */
public interface CancelOrderUseCase {

    /**
     * Cancela una orden pendiente validando el propietario.
     *
     * @param orderId identificador de la orden
     * @param requestedBy usuario que solicita la cancelacion
     * @return orden cancelada
     */
    TradeOrder cancel(OrderId orderId, String requestedBy);
}
