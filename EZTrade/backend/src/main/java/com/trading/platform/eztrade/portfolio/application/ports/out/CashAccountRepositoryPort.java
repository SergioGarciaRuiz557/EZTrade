package com.trading.platform.eztrade.portfolio.application.ports.out;

import com.trading.platform.eztrade.portfolio.domain.CashAccount;

import java.util.Optional;

public interface CashAccountRepositoryPort {

    Optional<CashAccount> findByOwner(String owner);

    CashAccount save(CashAccount cashAccount);
}

