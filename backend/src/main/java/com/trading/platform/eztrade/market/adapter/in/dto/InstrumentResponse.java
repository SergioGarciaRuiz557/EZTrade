package com.trading.platform.eztrade.market.adapter.in.dto;

import com.trading.platform.eztrade.market.domain.Instrument;

/**
 * REST DTO for an instrument search match.
 * <p>
 * Represents the compact information returned by Alpha Vantage when searching
 * by keyword: ticker, name, region, and currency.
 */
public record InstrumentResponse(
        String symbol,
        String name,
        String region,
        String currency
) {
    /** Maps the domain record to the controller's public contract. */
    public static InstrumentResponse from(Instrument instrument) {
        return new InstrumentResponse(
                instrument.symbol(),
                instrument.name(),
                instrument.region(),
                instrument.currency()
        );
    }
}
