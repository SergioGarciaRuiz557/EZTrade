package com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCashAccountRepository extends JpaRepository<CashAccountJpaEntity, Long> {

    Optional<CashAccountJpaEntity> findByOwner(String owner);
}

