package com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data repository for the available-cash projection by user.
 */
public interface SpringDataCashProjectionRepository extends JpaRepository<CashProjectionJpaEntity, Long> {

    /** Retrieves the single cash projection for the owner. */
    Optional<CashProjectionJpaEntity> findByOwner(String owner);
}

