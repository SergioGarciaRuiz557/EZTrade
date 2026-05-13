package com.trading.platform.eztrade.portfolio.adapter.out.persistence;

import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.PositionJpaEntity;
import com.trading.platform.eztrade.portfolio.adapter.out.persistence.jpa.SpringDataPositionRepository;
import com.trading.platform.eztrade.portfolio.application.ports.out.PositionRepositoryPort;
import com.trading.platform.eztrade.portfolio.domain.Position;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa el puerto de posiciones con Spring Data JPA.
 * <p>
 * Su funcion es traducir entre el dominio {@link Position} y
 * {@link PositionJpaEntity}, delegando las consultas reales en el repositorio
 * de infraestructura.
 */
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
        // Se conserva el id de la fila existente para que save() haga update en
        // lugar de insertar otra posicion con la misma clave owner+symbol.
        existing.ifPresent(entity -> toSave.setId(entity.getId()));
        return PositionMapper.toDomain(repository.save(toSave));
    }

    @Override
    public void deleteByOwnerAndSymbol(String owner, String symbol) {
        repository.deleteByOwnerAndSymbol(owner, symbol);
    }
}

