package com.trading.platform.eztrade.trading.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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
}
