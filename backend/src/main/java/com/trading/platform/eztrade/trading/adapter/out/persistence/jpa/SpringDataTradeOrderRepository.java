package com.trading.platform.eztrade.trading.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.trading.domain.OrderSide;
import com.trading.platform.eztrade.trading.domain.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link TradeOrderJpaEntity}.
 */
public interface SpringDataTradeOrderRepository extends JpaRepository<TradeOrderJpaEntity, Long> {

    /**
     * Retrieves every order whose owner matches the provided value.
     *
     * @param owner order owner
     * @return order entity list
     */
    List<TradeOrderJpaEntity> findByOwner(String owner);

    /**
     * Loads an order with a pessimistic write lock.
     * <p>
     * Used when buying a SELL offer to prevent two buyers from executing the
     * same pending offer at the same time.
     *
     * @param id id of the order to lock
     * @return found order, when it exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from TradeOrderJpaEntity o where o.id = :id")
    Optional<TradeOrderJpaEntity> findByIdForUpdate(@Param("id") Long id);

    /**
     * Finds all pending SELL offers that do not belong to the buyer.
     * <p>
     * Used to list the marketplace without a symbol filter.
     */
    List<TradeOrderJpaEntity> findBySideAndStatusAndOwnerNot(OrderSide side, OrderStatus status, String owner);

    /**
     * Finds pending SELL offers for a concrete symbol, excluding the buyer's own
     * offers.
     */
    List<TradeOrderJpaEntity> findBySideAndStatusAndSymbolAndOwnerNot(
            OrderSide side,
            OrderStatus status,
            String symbol,
            String owner
    );

    /**
     * Finds a seller's pending SELL offers for a symbol.
     * <p>
     * Used to calculate how many shares are already committed in offers.
     */
    List<TradeOrderJpaEntity> findBySideAndStatusAndOwnerAndSymbol(
            OrderSide side,
            OrderStatus status,
            String owner,
            String symbol
    );

    /**
     * Retrieves a user's executed orders for a symbol.
     * <p>
     * Trading uses them to calculate the net position without depending
     * directly on the portfolio module.
     */
    List<TradeOrderJpaEntity> findByStatusAndOwnerAndSymbol(OrderStatus status, String owner, String symbol);
}
