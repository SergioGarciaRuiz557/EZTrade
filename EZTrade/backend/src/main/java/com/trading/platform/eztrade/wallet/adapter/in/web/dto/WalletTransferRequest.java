package com.trading.platform.eztrade.wallet.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Input DTO for transferring available funds to another user.
 */
public record WalletTransferRequest(
        @NotBlank(message = "Recipient is required")
        @Size(max = 120, message = "Recipient max length is 120")
        String recipient,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.00000001", message = "Amount must be greater than zero")
        BigDecimal amount,
        @Size(max = 120, message = "Reference id max length is 120")
        String referenceId,
        @Size(max = 255, message = "Description max length is 255")
        String description
) {
}
