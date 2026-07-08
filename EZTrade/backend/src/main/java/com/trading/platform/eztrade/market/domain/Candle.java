package com.trading.platform.eztrade.market.domain;

import java.time.LocalDateTime;

/**
 * Represents a market candle for a specific time interval.
 * <p>
 * Each candle contains the typical OHLCV information:
 * <ul>
 *     <li><b>open</b>: interval opening price.</li>
 *     <li><b>high</b>: highest price reached in the interval.</li>
 *     <li><b>low</b>: lowest price reached in the interval.</li>
 *     <li><b>close</b>: interval closing price.</li>
 *     <li><b>volume</b>: volume traded in the interval.</li>
 * </ul>
 */
public record Candle(
        LocalDateTime time,
        double open,
        double high,
        double low,
        double close,
        long volume
) {}
