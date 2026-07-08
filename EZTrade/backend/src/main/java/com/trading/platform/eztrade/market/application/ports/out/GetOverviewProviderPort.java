package com.trading.platform.eztrade.market.application.ports.out;

import com.trading.platform.eztrade.market.domain.InstrumentOverview;
import com.trading.platform.eztrade.market.domain.Symbol;

/**
 * Output port that defines how to obtain a symbol's fundamental information
 * (overview) from an external source (market API, etc.).
 */
public interface GetOverviewProviderPort {
    InstrumentOverview getOverview(Symbol symbol);
}
