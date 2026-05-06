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
     * Busca una orden por id aplicando bloqueo de escritura en persistencia cuando
     * la infraestructura lo soporte.
     * <p>
     * En el marketplace se usa para bloquear una oferta SELL mientras se compra,
     * evitando que dos compradores la ejecuten simultaneamente.
     *
     * @param orderId identificador de la orden
     * @return optional con la orden si existe
     */
    Optional<TradeOrder> findByIdForUpdate(OrderId orderId);

    /**
     * Obtiene todas las ordenes de un propietario.
     *
     * @param owner propietario de las ordenes
     * @return lista de ordenes
     */
    List<TradeOrder> findByOwner(String owner);

    /**
     * Obtiene ordenes ejecutadas de un propietario para un simbolo.
     * <p>
     * Permite calcular dentro de trading la posicion neta de un usuario sin
     * introducir una dependencia directa hacia portfolio.
     *
     * @param owner propietario
     * @param symbol simbolo
     * @return ordenes ejecutadas del propietario para ese simbolo
     */
    List<TradeOrder> findExecutedOrdersByOwnerAndSymbol(String owner, String symbol);

    /**
     * Obtiene ofertas de venta pendientes para un simbolo, excluyendo al comprador.
     * <p>
     * Es la consulta base del listado del marketplace.
     *
     * @param symbol simbolo opcional; si es null o blanco se devuelven todos los simbolos
     * @param excludedOwner propietario a excluir
     * @return lista de ofertas de venta pendientes
     */
    List<TradeOrder> findPendingSellOffers(String symbol, String excludedOwner);

    /**
     * Obtiene las ofertas de venta pendientes de un propietario para un simbolo.
     * <p>
     * Se usa para descontar las acciones ya comprometidas en ofertas y no dejar
     * que un vendedor publique mas acciones de las que tiene disponibles.
     *
     * @param owner propietario
     * @param symbol simbolo
     * @return lista de ofertas pendientes del propietario
     */
    List<TradeOrder> findPendingSellOffersByOwnerAndSymbol(String owner, String symbol);
}
