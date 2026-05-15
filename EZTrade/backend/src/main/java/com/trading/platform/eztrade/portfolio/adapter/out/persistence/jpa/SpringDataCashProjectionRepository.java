package com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data para la proyeccion de cash disponible por usuario.
 */
public interface SpringDataCashProjectionRepository extends JpaRepository<CashProjectionJpaEntity, Long> {

    /** Recupera la proyeccion unica de cash para el owner. */
    Optional<CashProjectionJpaEntity> findByOwner(String owner);
}

