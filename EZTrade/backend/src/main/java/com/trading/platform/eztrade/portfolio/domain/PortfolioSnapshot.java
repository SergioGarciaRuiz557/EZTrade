package com.trading.platform.eztrade.portfolio.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Domain view of a user's portfolio.
 * <p>
 * Groups projected cash from wallet, open positions, cost basis, realized PnL,
 * and, for REST queries, market valuations by symbol.
 */
public record PortfolioSnapshot(
        String owner,
        BigDecimal cashAvailable,
        BigDecimal totalCostBasis,
        BigDecimal totalRealizedPnl,
        List<Position> positions,
        Map<String, PositionMarketValuation> marketValuations
) {

    public PortfolioSnapshot(String owner,
                             BigDecimal cashAvailable,
                             BigDecimal totalCostBasis,
                             BigDecimal totalRealizedPnl,
                             List<Position> positions) {
        this(owner, cashAvailable, totalCostBasis, totalRealizedPnl, positions, Map.of());
    }

    public PortfolioSnapshot {
        // Defensive copies: the snapshot must be a stable read-only picture.
        positions = List.copyOf(positions);
        marketValuations = Map.copyOf(marketValuations);
    }

    /**
     * Finds the market valuation by symbol, normalizing it to uppercase.
     */
    public Optional<PositionMarketValuation> marketValuationFor(String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(marketValuations.get(symbol.toUpperCase(Locale.ROOT)));
    }
}

