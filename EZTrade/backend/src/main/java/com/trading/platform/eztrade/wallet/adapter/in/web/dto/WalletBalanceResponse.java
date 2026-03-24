package com.trading.platform.eztrade.wallet.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * DTO de salida con el estado actual de balances del wallet.
 */
public record WalletBalanceResponse(
        String owner,
        BigDecimal availableBalance,
        BigDecimal reservedBalance
) {
}

