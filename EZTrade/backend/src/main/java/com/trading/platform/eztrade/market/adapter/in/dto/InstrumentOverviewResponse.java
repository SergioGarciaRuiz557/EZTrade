package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.InstrumentOverview;

/**
 * DTO REST con datos fundamentales resumidos de un instrumento.
 * <p>
 * La API expone una seleccion pequena y estable del overview externo:
 * identificacion, clasificacion sectorial, capitalizacion y PER.
 */
public record InstrumentOverviewResponse(
        String symbol,
        String name,
        String sector,
        String industry,
        long marketCap,
        double peRatio
) {
    /** Convierte el modelo de dominio en la respuesta consumible por frontend. */
    public static InstrumentOverviewResponse from(InstrumentOverview overview) {
        return new InstrumentOverviewResponse(
                overview.symbol(),
                overview.name(),
                overview.sector(),
                overview.industry(),
                overview.marketCap(),
                overview.peRatio()
        );
    }
}
