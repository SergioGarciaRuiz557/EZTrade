package com.trading.platform.eztrade.market.application.ports.in;

import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;

import java.util.List;

/**
 * Caso de uso para obtener todos los instrumentos disponibles.
 */
public interface GetPriceUserCase {

    MarketPrice getPrice(Symbol symbol);
}

