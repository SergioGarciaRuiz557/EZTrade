package com.trading.platform.eztrade.trading.adapter.out.persistence;

import com.trading.platform.eztrade.trading.adapter.out.persistence.jpa.SpringDataTradeOrderRepository;
import com.trading.platform.eztrade.trading.adapter.out.persistence.jpa.TradeOrderJpaEntity;
import com.trading.platform.eztrade.trading.application.ports.out.TradeOrderRepositoryPort;
import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.OrderSide;
import com.trading.platform.eztrade.trading.domain.OrderStatus;
import com.trading.platform.eztrade.trading.domain.TradeOrder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Persistence output adapter for the {@link TradeOrder} aggregate.
 * <p>
 * Implements {@link TradeOrderRepositoryPort} by delegating to Spring Data JPA.
 */
@Repository
public class TradeOrderRepositoryAdapter implements TradeOrderRepositoryPort {

    private final SpringDataTradeOrderRepository repository;

    /**
     * Constructor with the JPA repository.
     *
     * @param repository infrastructure repository
     */
    public TradeOrderRepositoryAdapter(SpringDataTradeOrderRepository repository) {
        this.repository = repository;
    }

    /**
     * Persists a domain order.
     *
     * @param order aggregate to persist
     * @return persisted aggregate
     */
    @Override
    public TradeOrder save(TradeOrder order) {
        TradeOrderJpaEntity saved = repository.save(TradeOrderMapper.toEntity(order));
        return TradeOrderMapper.toDomain(saved);
    }

    /**
     * Finds an order by id.
     *
     * @param orderId order identifier
     * @return optional containing the order when it exists
     */
    @Override
    public Optional<TradeOrder> findById(OrderId orderId) {
        return repository.findById(orderId.value()).map(TradeOrderMapper::toDomain);
    }

    @Override
    public Optional<TradeOrder> findByIdForUpdate(OrderId orderId) {
        // Delegate to a pessimistic-locking query to protect offer purchases
        // from concurrent executions.
        return repository.findByIdForUpdate(orderId.value()).map(TradeOrderMapper::toDomain);
    }

    /**
     * Finds all orders owned by a given user.
     *
     * @param owner order owner
     * @return order list
     */
    @Override
    public List<TradeOrder> findByOwner(String owner) {
        return repository.findByOwner(owner).stream().map(TradeOrderMapper::toDomain).toList();
    }

    @Override
    public List<TradeOrder> findExecutedOrdersByOwnerAndSymbol(String owner, String symbol) {
        // Normalize the symbol here because Spring Data derived methods compare
        // by exact equality against the stored database value.
        return repository.findByStatusAndOwnerAndSymbol(
                        OrderStatus.EXECUTED,
                        owner,
                        symbol.trim().toUpperCase(Locale.ROOT)
                )
                .stream()
                .map(TradeOrderMapper::toDomain)
                .toList();
    }

    @Override
    public List<TradeOrder> findPendingSellOffers(String symbol, String excludedOwner) {
        List<TradeOrderJpaEntity> offers;
        if (symbol == null || symbol.isBlank()) {
            // Without a symbol, all pending offers from other users are
            // returned. The application service decides whether additional
            // business filters are needed.
            offers = repository.findBySideAndStatusAndOwnerNot(OrderSide.SELL, OrderStatus.PENDING, excludedOwner);
        } else {
            // With a symbol, the result is reduced in the database so the
            // marketplace does not fetch unnecessary offers.
            offers = repository.findBySideAndStatusAndSymbolAndOwnerNot(
                    OrderSide.SELL,
                    OrderStatus.PENDING,
                    symbol.trim().toUpperCase(Locale.ROOT),
                    excludedOwner
            );
        }
        return offers.stream().map(TradeOrderMapper::toDomain).toList();
    }

    @Override
    public List<TradeOrder> findPendingSellOffersByOwnerAndSymbol(String owner, String symbol) {
        // Supporting query to calculate shares already committed by a seller in
        // pending SELL offers.
        return repository.findBySideAndStatusAndOwnerAndSymbol(
                        OrderSide.SELL,
                        OrderStatus.PENDING,
                        owner,
                        symbol.trim().toUpperCase(Locale.ROOT)
                )
                .stream()
                .map(TradeOrderMapper::toDomain)
                .toList();
    }
}
