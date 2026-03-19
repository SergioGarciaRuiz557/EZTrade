package com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataPositionRepository extends JpaRepository<PositionJpaEntity, Long> {

    Optional<PositionJpaEntity> findByOwnerAndSymbol(String owner, String symbol);

    List<PositionJpaEntity> findByOwner(String owner);

    void deleteByOwnerAndSymbol(String owner, String symbol);
}

