package com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCashProjectionRepository extends JpaRepository<CashProjectionJpaEntity, Long> {

    Optional<CashProjectionJpaEntity> findByOwner(String owner);
}

