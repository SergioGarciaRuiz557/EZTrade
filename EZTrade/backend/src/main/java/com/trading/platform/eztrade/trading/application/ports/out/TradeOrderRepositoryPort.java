package com.trading.platform.eztrade.trading.application.ports.out;

import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia/consulta de ordenes.
 * <p>
 * Su implementacion concreta pertenece a la capa de adaptadores.
 */
public interface TradeOrderRepositoryPort {

    /**
     * Guarda una orden nueva o existente.
     *
     * @param order agregado a persistir
     * @return agregado persistido
     */
    TradeOrder save(TradeOrder order);

    /**
     * Busca una orden por id.
     *
     * @param orderId identificador de la orden
     * @return optional con la orden si existe
     */
    Optional<TradeOrder> findById(OrderId orderId);

    /**
     * Obtiene todas las ordenes de un propietario.
     *
     * @param owner propietario de las ordenes
     * @return lista de ordenes
     */
    List<TradeOrder> findByOwner(String owner);
}
