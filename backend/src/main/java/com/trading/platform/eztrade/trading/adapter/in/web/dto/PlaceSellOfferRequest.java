package com.trading.platform.eztrade.trading.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Input DTO for publishing shares for sale inside the platform.
 * <p>
 * The user-provided price is validated in the application layer so it does not
 * exceed the current market price.
 *
 * @param symbol ticker of the offered share
 * @param quantity number of shares placed for sale
 * @param price seller's desired unit price
 */
public record PlaceSellOfferRequest(
        @NotBlank(message = "Symbol is required")
        String symbol,
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than zero")
        BigDecimal quantity,
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        BigDecimal price
) {
}
