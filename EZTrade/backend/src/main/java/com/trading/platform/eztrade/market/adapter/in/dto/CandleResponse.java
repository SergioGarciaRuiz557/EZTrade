package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.Candle;

import java.time.LocalDateTime;

/**
 * REST DTO for a historical daily candle.
 * <p>
 * Keeps the OHLCV structure consumed by the frontend: open, high, low, close,
 * and volume for a time instant.
 */
public record CandleResponse(
        LocalDateTime time,
        double open,
        double high,
        double low,
        double close,
        long volume
) {
    /** Transforms the domain candle into the HTTP contract. */
    public static CandleResponse from(Candle candle) {
        return new CandleResponse(
                candle.time(),
                candle.open(),
                candle.high(),
                candle.low(),
                candle.close(),
                candle.volume()
        );
    }
}
