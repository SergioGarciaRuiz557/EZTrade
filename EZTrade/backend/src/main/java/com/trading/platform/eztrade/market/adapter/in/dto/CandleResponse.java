package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.Candle;

import java.time.LocalDateTime;

public record CandleResponse(
        LocalDateTime time,
        double open,
        double high,
        double low,
        double close,
        long volume
) {
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
