package com.trading.platform.eztrade.wallet.application.ports.in;

import com.trading.platform.eztrade.wallet.domain.MovementType;
import com.trading.platform.eztrade.wallet.domain.ReferenceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Input port for querying the wallet movement history.
 */
public interface GetWalletTransactionsUseCase {

    List<TransactionView> getTransactions(String owner);

    record TransactionView(
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
}
