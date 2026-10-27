package com.trading.platform.eztrade.portfolio.application.ports.in;

import com.trading.platform.eztrade.portfolio.domain.PortfolioSnapshot;

/**
 * Input port for querying a user's aggregate portfolio.
 * <p>
 * Used by the REST adapter; keeps the composition of positions, projected cash,
 * and market valuations outside the controller.
 */
public interface GetPortfolioUseCase {

    /** Returns the current portfolio picture for the given owner. */
    PortfolioSnapshot getByOwner(String owner);
}

