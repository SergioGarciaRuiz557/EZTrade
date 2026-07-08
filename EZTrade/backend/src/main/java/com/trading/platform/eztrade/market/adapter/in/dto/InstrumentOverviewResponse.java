package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.InstrumentOverview;

/**
 * REST DTO with summarized fundamentals for an instrument.
 * <p>
 * The API exposes a small and stable selection of the external overview:
 * identification, sector classification, market capitalization, and P/E ratio.
 */
public record InstrumentOverviewResponse(
        String symbol,
        String name,
        String sector,
        String industry,
        long marketCap,
        double peRatio
) {
    /** Converts the domain model into the response consumed by the frontend. */
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
