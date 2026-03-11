package com.trading.platform.eztrade.trading.adapter.out.persistence;

import com.trading.platform.eztrade.trading.adapter.out.persistence.jpa.SpringDataTradeOrderRepository;
import com.trading.platform.eztrade.trading.adapter.out.persistence.jpa.TradeOrderJpaEntity;
import com.trading.platform.eztrade.trading.application.ports.out.TradeOrderRepositoryPort;
import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.TradeOrder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida de persistencia para el agregado {@link TradeOrder}.
 * <p>
 * Implementa el puerto {@link TradeOrderRepositoryPort} delegando en Spring Data JPA.
 */
@Repository
public class TradeOrderRepositoryAdapter implements TradeOrderRepositoryPort {

    private final SpringDataTradeOrderRepository repository;

    /**
     * Constructor con repositorio JPA.
     *
     * @param repository repositorio de infraestructura
     */
    public TradeOrderRepositoryAdapter(SpringDataTradeOrderRepository repository) {
        this.repository = repository;
    }

    /**
     * Persiste una orden de dominio.
     *
     * @param order agregado a persistir
     * @return agregado persistido
     */
    @Override
    public TradeOrder save(TradeOrder order) {
        TradeOrderJpaEntity saved = repository.save(TradeOrderMapper.toEntity(order));
        return TradeOrderMapper.toDomain(saved);
    }

    /**
     * Busca una orden por id.
     *
     * @param orderId identificador de la orden
     * @return optional con la orden si existe
     */
    @Override
    public Optional<TradeOrder> findById(OrderId orderId) {
        return repository.findById(orderId.value()).map(TradeOrderMapper::toDomain);
    }

    /**
     * Busca todas las ordenes de un propietario.
     *
     * @param owner propietario de las ordenes
     * @return lista de ordenes
     */
    @Override
    public List<TradeOrder> findByOwner(String owner) {
        return repository.findByOwner(owner).stream().map(TradeOrderMapper::toDomain).toList();
    }
}
