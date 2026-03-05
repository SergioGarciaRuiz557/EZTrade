package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.InstrumentOverview;
import com.trading.platform.eztrade.market.domain.Symbol;

/**
 * Puerto de salida que define cómo obtener la información fundamental
 * (overview) de un símbolo desde una fuente externa (API de mercado, etc.).
 */
public interface GetOverviewProviderPort {
    InstrumentOverview getOverview(Symbol symbol);
}
