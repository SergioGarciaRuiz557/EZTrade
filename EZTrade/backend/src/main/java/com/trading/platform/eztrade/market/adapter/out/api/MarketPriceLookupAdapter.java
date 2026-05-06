package com.trading.platform.eztrade.market.adapter.out.api;

import com.trading.platform.eztrade.market.api.MarketPriceLookupPort;
import com.trading.platform.eztrade.market.application.ports.in.GetPriceUserCase;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adaptador que implementa la API publica de precios de market.
 * <p>
 * Su funcion es traducir la llamada simple de otros modulos
 * ({@link MarketPriceLookupPort#currentPrice(String)}) al caso de uso interno
 * de market ({@link GetPriceUserCase}). Asi trading puede validar precios
 * contra AlphaVantage sin acoplarse a controladores ni proveedores externos.
 */
@Component
class MarketPriceLookupAdapter implements MarketPriceLookupPort {

    private final GetPriceUserCase getPriceUserCase;

    MarketPriceLookupAdapter(GetPriceUserCase getPriceUserCase) {
        this.getPriceUserCase = getPriceUserCase;
    }

    @Override
    public BigDecimal currentPrice(String symbol) {
        // Reutilizamos el value object Symbol para mantener las mismas reglas
        // de normalizacion y validacion que el resto del modulo market.
        MarketPrice marketPrice = getPriceUserCase.getPrice(Symbol.of(symbol));
        return BigDecimal.valueOf(marketPrice.price());
    }
}
