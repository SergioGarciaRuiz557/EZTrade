package com.trading.platform.eztrade.portfolio.application.ports.out;

import com.trading.platform.eztrade.portfolio.domain.CashProjection;

import java.util.Optional;

/**
 * Output port for persisting the local available-cash projection.
 */
public interface CashProjectionRepositoryPort {

    Optional<CashProjection> findByOwner(String owner);

    CashProjection save(CashProjection projection);
}

