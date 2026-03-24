package com.trading.platform.eztrade.wallet.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO de entrada para depositar fondos en el wallet del usuario autenticado.
 */
public record WalletDepositRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.00000001", message = "Amount must be greater than zero")
        BigDecimal amount,
        @Size(max = 120, message = "Reference id max length is 120")
        String referenceId,
        @Size(max = 255, message = "Description max length is 255")
        String description
) {
}

