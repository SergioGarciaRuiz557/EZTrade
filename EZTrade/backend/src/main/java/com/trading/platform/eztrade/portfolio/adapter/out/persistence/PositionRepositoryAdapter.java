package com.trading.platform.eztrade.portfolio.adapter.out.persistence;

import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.PositionJpaEntity;
import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.SpringDataPositionRepository;
import com.trading.platform.eztrade.portfolio.application.ports.out.PositionRepositoryPort;
import com.trading.platform.eztrade.portfolio.domain.Position;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PositionRepositoryAdapter implements PositionRepositoryPort {

    private final SpringDataPositionRepository repository;

    public PositionRepositoryAdapter(SpringDataPositionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Position> findByOwnerAndSymbol(String owner, String symbol) {
        return repository.findByOwnerAndSymbol(owner, symbol).map(PositionMapper::toDomain);
    }

    @Override
    public List<Position> findByOwner(String owner) {
        return repository.findByOwner(owner).stream().map(PositionMapper::toDomain).toList();
    }

    @Override
    public Position save(Position position) {
        Optional<PositionJpaEntity> existing = repository.findByOwnerAndSymbol(position.owner(), position.symbol());
        PositionJpaEntity toSave = PositionMapper.toEntity(position);
        existing.ifPresent(entity -> toSave.setId(entity.getId()));
        return PositionMapper.toDomain(repository.save(toSave));
    }

    @Override
    public void deleteByOwnerAndSymbol(String owner, String symbol) {
        repository.deleteByOwnerAndSymbol(owner, symbol);
    }
}

