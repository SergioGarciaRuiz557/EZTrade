package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.Candle;

import java.time.LocalDateTime;

/**
 * DTO REST para una vela historica diaria.
 * <p>
 * Mantiene la estructura OHLCV que consume el frontend: apertura, maximo,
 * minimo, cierre y volumen para un instante temporal.
 */
public record CandleResponse(
        LocalDateTime time,
        double open,
        double high,
        double low,
        double close,
        long volume
) {
    /** Transforma la vela de dominio en el contrato HTTP. */
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
