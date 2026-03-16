package com.trading.platform.eztrade.portfolio.application.ports.in;

import com.trading.platform.eztrade.portfolio.domain.PortfolioSnapshot;

public interface GetPortfolioUseCase {

    PortfolioSnapshot getByOwner(String owner);
}

