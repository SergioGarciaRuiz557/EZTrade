package com.trading.platform.eztrade.wallet.application.ports.in;

import java.math.BigDecimal;

public interface AdjustWalletFundsUseCase {

    void deposit(AdjustCommand command);

    void withdraw(AdjustCommand command);

    void chargeFee(AdjustCommand command);

    record AdjustCommand(String owner, BigDecimal amount, String referenceId, String description) {
    }
}

