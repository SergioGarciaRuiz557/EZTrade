package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.InstrumentOverview;
import com.trading.platform.eztrade.market.domain.Symbol;

/**
 * Use case for obtaining the fundamental information (overview) of a specific
 * market symbol.
 */
public interface GetOverviewUserCase {
    InstrumentOverview getOverview(Symbol symbol);
}
