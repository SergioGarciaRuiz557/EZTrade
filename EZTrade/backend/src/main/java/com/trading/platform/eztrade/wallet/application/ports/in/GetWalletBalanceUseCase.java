package com.trading.platform.eztrade.wallet.application.ports.in;

import java.math.BigDecimal;

/**
 * Puerto de entrada para consultar el estado actual del wallet de un usuario.
 */
public interface GetWalletBalanceUseCase {

    /** Devuelve los balances actuales del owner solicitado. */
    BalanceView getBalance(String owner);

    /** Vista de solo lectura del saldo del wallet. */
    record BalanceView(BigDecimal availableBalance, BigDecimal reservedBalance) {
    }
}

