package com.trading.platform.eztrade.wallet.application.ports.in;

import java.math.BigDecimal;

/**
 * Input port for querying a user's current wallet state.
 */
public interface GetWalletBalanceUseCase {

    /** Returns the current balances for the requested owner. */
    BalanceView getBalance(String owner);

    /** Read-only wallet balance view. */
    record BalanceView(BigDecimal availableBalance, BigDecimal reservedBalance) {
    }
}

