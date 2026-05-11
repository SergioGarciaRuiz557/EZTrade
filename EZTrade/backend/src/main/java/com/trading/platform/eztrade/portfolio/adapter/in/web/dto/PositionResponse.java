package com.trading.platform.eztrade.portfolio.adapter.in.web.dto;

import com.trading.platform.eztrade.portfolio.domain.Position;
import com.trading.platform.eztrade.portfolio.domain.PositionMarketValuation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DTO de salida para exponer una posicion del portfolio.
 */
public record PositionResponse(
        String symbol,
        BigDecimal quantity,
        BigDecimal averageCost,
        BigDecimal currentPrice,
        BigDecimal marketValue,
        BigDecimal unrealizedPnl,
        BigDecimal realizedPnl,
        LocalDateTime updatedAt
) {

    public static PositionResponse from(Position position) {
        return from(position, Optional.empty());
    }

    public static PositionResponse from(Position position, Optional<PositionMarketValuation> marketValuation) {
        BigDecimal currentPrice = marketValuation.map(PositionMarketValuation::currentPrice).orElse(null);
        BigDecimal marketValue = marketValuation.map(PositionMarketValuation::marketValue).orElse(position.investedAmount());
        BigDecimal positionPnl = marketValuation.map(PositionMarketValuation::unrealizedPnl).orElse(position.realizedPnl());

        return new PositionResponse(
                position.symbol(),
                position.quantity(),
                position.averageCost(),
                currentPrice,
                marketValue,
                positionPnl,
                positionPnl,
                position.updatedAt()
        );
    }
}

