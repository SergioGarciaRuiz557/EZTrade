package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

/**
 * Input port for executing an existing order.
 */
public interface ExecuteOrderUseCase {

    /**
     * Executes a pending order.
     *
     * @param orderId order identifier
     * @return executed order
     */
    TradeOrder execute(OrderId orderId);
}
