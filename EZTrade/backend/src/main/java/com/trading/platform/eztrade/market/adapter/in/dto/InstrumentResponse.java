package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.Instrument;

public record InstrumentResponse(
        String symbol,
        String name,
        String region,
        String currency
) {
    public static InstrumentResponse from(Instrument instrument) {
        return new InstrumentResponse(
                instrument.symbol(),
                instrument.name(),
                instrument.region(),
                instrument.currency()
        );
    }
}
