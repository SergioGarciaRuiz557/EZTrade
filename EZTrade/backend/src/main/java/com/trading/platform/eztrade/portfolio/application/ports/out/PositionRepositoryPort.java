package com.trading.platform.eztrade.portfolio.application.ports.out;

import com.trading.platform.eztrade.portfolio.domain.Position;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar posiciones de portfolio.
 * <p>
 * La aplicacion trabaja con {@link Position}; los detalles JPA quedan en el
 * adaptador de infraestructura.
 */
public interface PositionRepositoryPort {

    /** Busca la posicion unica de un usuario para un simbolo. */
    Optional<Position> findByOwnerAndSymbol(String owner, String symbol);

    /** Recupera todas las posiciones historicas/actuales de un usuario. */
    List<Position> findByOwner(String owner);

    /** Inserta o actualiza una posicion y devuelve el estado persistido. */
    Position save(Position position);

    /** Elimina la posicion identificada por owner y simbolo. */
    void deleteByOwnerAndSymbol(String owner, String symbol);
}

