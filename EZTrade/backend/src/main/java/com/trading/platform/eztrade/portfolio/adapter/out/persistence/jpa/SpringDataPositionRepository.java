package com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data para la tabla de posiciones de portfolio.
 * <p>
 * Se mantiene en infraestructura; la aplicacion lo consume a traves de
 * {@code PositionRepositoryPort}.
 */
public interface SpringDataPositionRepository extends JpaRepository<PositionJpaEntity, Long> {

    /** Localiza la posicion unica de un owner para un simbolo. */
    Optional<PositionJpaEntity> findByOwnerAndSymbol(String owner, String symbol);

    /** Devuelve todas las posiciones registradas para un owner. */
    List<PositionJpaEntity> findByOwner(String owner);

    /** Borra la fila de una posicion concreta por clave de negocio. */
    void deleteByOwnerAndSymbol(String owner, String symbol);
}

