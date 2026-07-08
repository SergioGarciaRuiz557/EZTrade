package com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for the portfolio positions table.
 * <p>
 * It stays in infrastructure; the application consumes it through
 * {@code PositionRepositoryPort}.
 */
public interface SpringDataPositionRepository extends JpaRepository<PositionJpaEntity, Long> {

    /** Locates the single owner position for a symbol. */
    Optional<PositionJpaEntity> findByOwnerAndSymbol(String owner, String symbol);

    /** Returns all positions registered for an owner. */
    List<PositionJpaEntity> findByOwner(String owner);

    /** Deletes a concrete position row by business key. */
    void deleteByOwnerAndSymbol(String owner, String symbol);
}

