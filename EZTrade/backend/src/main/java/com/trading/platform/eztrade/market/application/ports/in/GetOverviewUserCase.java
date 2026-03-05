package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.InstrumentOverview;
import com.trading.platform.eztrade.market.domain.Symbol;

/**
 * Caso de uso para obtener la información fundamental (overview) de un símbolo
 * de mercado concreto.
 */
public interface GetOverviewUserCase {
    InstrumentOverview getOverview(Symbol symbol);
}
