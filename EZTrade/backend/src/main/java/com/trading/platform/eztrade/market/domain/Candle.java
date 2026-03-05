package com.trading.platform.eztrade.market.domain;

import java.time.LocalDateTime;

/**
 * Representa una vela (candle) de mercado para un intervalo de tiempo concreto.
 * <p>
 * Cada vela contiene la información típica OHLCV:
 * <ul>
 *     <li><b>open</b>: precio de apertura del intervalo.</li>
 *     <li><b>high</b>: precio máximo alcanzado en el intervalo.</li>
 *     <li><b>low</b>: precio mínimo alcanzado en el intervalo.</li>
 *     <li><b>close</b>: precio de cierre del intervalo.</li>
 *     <li><b>volume</b>: volumen negociado en el intervalo.</li>
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