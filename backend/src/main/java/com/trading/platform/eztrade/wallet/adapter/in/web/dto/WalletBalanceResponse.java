package com.trading.platform.eztrade.wallet.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * Output DTO with the wallet's current balance state.
 */
public record WalletBalanceResponse(
        String owner,
        BigDecimal availableBalance,
        BigDecimal reservedBalance
) {
}

