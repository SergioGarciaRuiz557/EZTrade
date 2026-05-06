package com.trading.platform.eztrade.market.api;

import org.springframework.modulith.NamedInterface;

import java.math.BigDecimal;

/**
 * API publica del modulo market para que otros modulos puedan consultar el
 * precio actual de un simbolo sin depender de los servicios internos de market.
 * <p>
 * Se marca como {@link NamedInterface} porque Spring Modulith solo permite que
 * otros modulos dependan de interfaces publicas declaradas explicitamente.
 */
@NamedInterface
public interface MarketPriceLookupPort {

    /**
     * Devuelve el precio actual de mercado del simbolo indicado.
     * <p>
     * En la implementacion real este precio sale del flujo ya existente de
     * market, que consulta AlphaVantage y aplica su cache.
     *
     * @param symbol ticker normalizado o normalizable, por ejemplo AAPL
     * @return precio actual como {@link BigDecimal} para reglas monetarias
     */
    BigDecimal currentPrice(String symbol);
}
