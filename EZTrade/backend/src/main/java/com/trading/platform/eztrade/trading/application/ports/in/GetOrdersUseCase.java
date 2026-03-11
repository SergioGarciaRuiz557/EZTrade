package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.util.List;

/**
 * Puerto de entrada para consultas de ordenes.
 */
public interface GetOrdersUseCase {

    /**
     * Obtiene una orden por su identificador.
     *
     * @param orderId id de la orden
     * @return orden encontrada
     */
    TradeOrder getById(OrderId orderId);

    /**
     * Obtiene todas las ordenes de un propietario.
     *
     * @param owner propietario de las ordenes
     * @return lista de ordenes del propietario
     */
    List<TradeOrder> getByOwner(String owner);
}
