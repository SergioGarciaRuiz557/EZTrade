package com.trading.platform.eztrade.portfolio.adapter.in.web.dto;

import com.trading.platform.eztrade.portfolio.domain.PortfolioSnapshot;

import java.math.BigDecimal;
import java.util.List;

/**
 * Output DTO for exposing a user's portfolio view.
 */
public record PortfolioResponse(
        String owner,
        BigDecimal cashAvailable,
        BigDecimal totalCostBasis,
        BigDecimal totalRealizedPnl,
        List<PositionResponse> positions
) {

    public static PortfolioResponse from(PortfolioSnapshot snapshot) {
        return new PortfolioResponse(
                snapshot.owner(),
                snapshot.cashAvailable(),
                snapshot.totalCostBasis(),
                snapshot.totalRealizedPnl(),
                snapshot.positions().stream()
                        .map(position -> PositionResponse.from(position, snapshot.marketValuationFor(position.symbol())))
                        .toList()
        );
    }
}

