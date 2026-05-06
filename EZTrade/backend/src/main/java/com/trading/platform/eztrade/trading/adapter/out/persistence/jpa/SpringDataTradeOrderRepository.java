package com.trading.platform.eztrade.trading.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.trading.domain.OrderSide;
import com.trading.platform.eztrade.trading.domain.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link TradeOrderJpaEntity}.
 */
public interface SpringDataTradeOrderRepository extends JpaRepository<TradeOrderJpaEntity, Long> {

    /**
     * Recupera todas las ordenes cuyo propietario coincide con el valor indicado.
     *
     * @param owner propietario de las ordenes
     * @return lista de entidades de orden
     */
    List<TradeOrderJpaEntity> findByOwner(String owner);

    /**
     * Carga una orden con bloqueo pesimista de escritura.
     * <p>
     * Se usa al comprar una oferta SELL para evitar que dos compradores ejecuten
     * la misma oferta pendiente al mismo tiempo.
     *
     * @param id id de la orden que se quiere bloquear
     * @return orden encontrada, si existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from TradeOrderJpaEntity o where o.id = :id")
    Optional<TradeOrderJpaEntity> findByIdForUpdate(@Param("id") Long id);

    /**
     * Busca todas las ofertas SELL pendientes que no pertenecen al comprador.
     * <p>
     * Se usa para listar el marketplace sin filtro de simbolo.
     */
    List<TradeOrderJpaEntity> findBySideAndStatusAndOwnerNot(OrderSide side, OrderStatus status, String owner);

    /**
     * Busca ofertas SELL pendientes de un simbolo concreto excluyendo al propio
     * comprador.
     */
    List<TradeOrderJpaEntity> findBySideAndStatusAndSymbolAndOwnerNot(
            OrderSide side,
            OrderStatus status,
            String symbol,
            String owner
    );

    /**
     * Busca ofertas SELL pendientes de un vendedor para un simbolo.
     * <p>
     * Sirve para calcular cuantas acciones tiene ya comprometidas en ofertas.
     */
    List<TradeOrderJpaEntity> findBySideAndStatusAndOwnerAndSymbol(
            OrderSide side,
            OrderStatus status,
            String owner,
            String symbol
    );

    /**
     * Recupera las ordenes ejecutadas de un usuario para un simbolo.
     * <p>
     * Trading las usa para calcular la posicion neta sin depender directamente
     * del modulo portfolio.
     */
    List<TradeOrderJpaEntity> findByStatusAndOwnerAndSymbol(OrderStatus status, String owner, String symbol);
}
