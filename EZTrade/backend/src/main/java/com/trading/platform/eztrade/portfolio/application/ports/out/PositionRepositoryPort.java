package com.trading.platform.eztrade.portfolio.application.ports.out;

import com.trading.platform.eztrade.portfolio.domain.Position;

import java.util.List;
import java.util.Optional;

public interface PositionRepositoryPort {

    Optional<Position> findByOwnerAndSymbol(String owner, String symbol);

    List<Position> findByOwner(String owner);

    Position save(Position position);

    void deleteByOwnerAndSymbol(String owner, String symbol);
}

