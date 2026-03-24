package com.trading.platform.eztrade.portfolio.application.ports.out;

import com.trading.platform.eztrade.portfolio.domain.CashProjection;

import java.util.Optional;

/**
 * Puerto de salida para persistir la proyeccion local de cash disponible.
 */
public interface CashProjectionRepositoryPort {

    Optional<CashProjection> findByOwner(String owner);

    CashProjection save(CashProjection projection);
}

