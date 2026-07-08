package com.trading.platform.eztrade.portfolio.application.ports.out;

import com.trading.platform.eztrade.portfolio.domain.Position;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting and querying portfolio positions.
 * <p>
 * The application works with {@link Position}; JPA details remain in the
 * infrastructure adapter.
 */
public interface PositionRepositoryPort {

    /** Finds the single user position for a symbol. */
    Optional<Position> findByOwnerAndSymbol(String owner, String symbol);

    /** Retrieves all historical/current positions for a user. */
    List<Position> findByOwner(String owner);

    /** Inserts or updates a position and returns the persisted state. */
    Position save(Position position);

    /** Deletes the position identified by owner and symbol. */
    void deleteByOwnerAndSymbol(String owner, String symbol);
}

