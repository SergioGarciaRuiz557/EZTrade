package com.trading.platform.eztrade.trading.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Input DTO for buying shares directly from the market.
 * <p>
 * It does not include a price because the backend obtains it from AlphaVantage
 * through the market module. This prevents the client from manipulating the
 * purchase price.
 *
 * @param symbol ticker of the share to buy
 * @param quantity requested number of shares
 */
public record BuyFromMarketRequest(
        @NotBlank(message = "Symbol is required")
        String symbol,
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than zero")
        BigDecimal quantity
) {
}
