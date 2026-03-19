package com.trading.platform.eztrade.portfolio.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vista de dominio de la cartera de un usuario.
 */
public record PortfolioSnapshot(
        String owner,
        BigDecimal cashAvailable,
        BigDecimal totalCostBasis,
        BigDecimal totalRealizedPnl,
        List<Position> positions
) {
}

