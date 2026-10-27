package com.trading.platform.eztrade.wallet.adapter.in.web.dto;

import com.trading.platform.eztrade.wallet.domain.MovementType;
import com.trading.platform.eztrade.wallet.domain.ReferenceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Output DTO for wallet historical movements.
 */
public record WalletTransactionResponse(
        Long id,
        MovementType movementType,
        BigDecimal amount,
        BigDecimal availableDelta,
        BigDecimal reservedDelta,
        BigDecimal availableBalanceAfter,
        BigDecimal reservedBalanceAfter,
        ReferenceType referenceType,
        String referenceId,
        String description,
        LocalDateTime occurredAt
) {
}
