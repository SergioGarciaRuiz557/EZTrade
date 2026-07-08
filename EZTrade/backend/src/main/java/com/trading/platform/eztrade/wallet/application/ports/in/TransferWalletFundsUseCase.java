package com.trading.platform.eztrade.wallet.application.ports.in;

import java.math.BigDecimal;

/**
 * Input port for transferring available funds between wallets.
 */
public interface TransferWalletFundsUseCase {

    void transfer(TransferCommand command);

    record TransferCommand(String owner,
                           String recipientOwner,
                           BigDecimal amount,
                           String referenceId,
                           String description) {
    }
}
