package com.trading.platform.eztrade.portfolio.adapter.out.persistence;

import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.CashProjectionJpaEntity;
import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.SpringDataCashProjectionRepository;
import com.trading.platform.eztrade.portfolio.application.ports.out.CashProjectionRepositoryPort;
import com.trading.platform.eztrade.portfolio.domain.CashProjection;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CashProjectionRepositoryAdapter implements CashProjectionRepositoryPort {

    private final SpringDataCashProjectionRepository repository;

    public CashProjectionRepositoryAdapter(SpringDataCashProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CashProjection> findByOwner(String owner) {
        return repository.findByOwner(owner).map(CashProjectionMapper::toDomain);
    }

    @Override
    public CashProjection save(CashProjection projection) {
        Optional<CashProjectionJpaEntity> existing = repository.findByOwner(projection.owner());
        CashProjectionJpaEntity toSave = CashProjectionMapper.toEntity(projection);
        existing.ifPresent(entity -> toSave.setId(entity.getId()));
        return CashProjectionMapper.toDomain(repository.save(toSave));
    }
}

