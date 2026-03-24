package com.trading.platform.eztrade.portfolio.adapter.in.web.dto;

import com.trading.platform.eztrade.portfolio.domain.Position;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida para exponer una posicion del portfolio.
 */
public record PositionResponse(
        String symbol,
        BigDecimal quantity,
        BigDecimal averageCost,
        BigDecimal realizedPnl,
        LocalDateTime updatedAt
) {

    public static PositionResponse from(Position position) {
        return new PositionResponse(
                position.symbol(),
                position.quantity(),
                position.averageCost(),
                position.realizedPnl(),
                position.updatedAt()
        );
    }
}

