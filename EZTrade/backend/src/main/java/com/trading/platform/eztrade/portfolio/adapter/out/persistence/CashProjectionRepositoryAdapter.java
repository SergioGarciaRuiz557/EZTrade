package com.trading.platform.eztrade.portfolio.adapter.out.persistence;

import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.CashProjectionJpaEntity;
import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.SpringDataCashProjectionRepository;
import com.trading.platform.eztrade.portfolio.application.ports.out.CashProjectionRepositoryPort;
import com.trading.platform.eztrade.portfolio.domain.CashProjection;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador de salida para la proyeccion local de cash de portfolio.
 * <p>
 * La proyeccion se actualiza a partir de eventos de wallet y se guarda por
 * owner para poder componer rapidamente el snapshot de cartera.
 */
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
        // Owner es unico; si ya existe proyeccion, conservamos su id para actualizar.
        existing.ifPresent(entity -> toSave.setId(entity.getId()));
        return CashProjectionMapper.toDomain(repository.save(toSave));
    }
}

