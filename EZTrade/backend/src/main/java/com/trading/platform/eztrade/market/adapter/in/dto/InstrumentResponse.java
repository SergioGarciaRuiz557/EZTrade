package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.Instrument;

/**
 * DTO REST para una coincidencia de busqueda de instrumentos.
 * <p>
 * Representa la informacion compacta devuelta por AlphaVantage al buscar por
 * palabra clave: ticker, nombre, region y moneda.
 */
public record InstrumentResponse(
        String symbol,
        String name,
        String region,
        String currency
) {
    /** Mapea el record de dominio al contrato publico del controlador. */
    public static InstrumentResponse from(Instrument instrument) {
        return new InstrumentResponse(
                instrument.symbol(),
                instrument.name(),
                instrument.region(),
                instrument.currency()
        );
    }
}
