package com.trading.platform.eztrade.portfolio.application.ports.in;

import com.trading.platform.eztrade.portfolio.domain.PortfolioSnapshot;

/**
 * Puerto de entrada para consultar la cartera agregada de un usuario.
 * <p>
 * Lo usa el adaptador REST y mantiene fuera del controlador la composicion de
 * posiciones, cash proyectado y valoraciones de mercado.
 */
public interface GetPortfolioUseCase {

    /** Devuelve la foto actual del portfolio para el owner indicado. */
    PortfolioSnapshot getByOwner(String owner);
}

