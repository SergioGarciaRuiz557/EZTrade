package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.InstrumentOverview;

public record InstrumentOverviewResponse(
        String symbol,
        String name,
        String sector,
        String industry,
        long marketCap,
        double peRatio
) {
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
