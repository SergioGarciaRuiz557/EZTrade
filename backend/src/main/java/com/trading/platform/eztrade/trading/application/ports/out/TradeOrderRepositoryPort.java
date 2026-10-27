package com.trading.platform.eztrade.trading.application.ports.out;

import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.util.List;
import java.util.Optional;

/**
 * Output port for order persistence/querying.
 * <p>
 * Its concrete implementation belongs to the adapter layer.
 */
public interface TradeOrderRepositoryPort {

    /**
     * Saves a new or existing order.
     *
     * @param order aggregate to persist
     * @return persisted aggregate
     */
    TradeOrder save(TradeOrder order);

    /**
     * Finds an order by id.
     *
     * @param orderId order identifier
     * @return optional with the order if it exists
     */
    Optional<TradeOrder> findById(OrderId orderId);

    /**
     * Finds an order by id while applying a persistence write lock when the
     * infrastructure supports it.
     * <p>
     * In the marketplace, this locks a SELL offer while it is being bought,
     * preventing two buyers from executing it simultaneously.
     *
     * @param orderId order identifier
     * @return optional with the order if it exists
     */
    Optional<TradeOrder> findByIdForUpdate(OrderId orderId);

    /**
     * Gets all orders for an owner.
     *
     * @param owner order owner
     * @return order list
     */
    List<TradeOrder> findByOwner(String owner);

    /**
     * Gets an owner's executed orders for a symbol.
     * <p>
     * Allows trading to calculate a user's net position without introducing a
     * direct dependency on portfolio.
     *
     * @param owner owner
     * @param symbol symbol
     * @return owner's executed orders for that symbol
     */
    List<TradeOrder> findExecutedOrdersByOwnerAndSymbol(String owner, String symbol);

    /**
     * Gets pending sell offers for a symbol, excluding the buyer.
     * <p>
     * This is the base query for the marketplace listing.
     *
     * @param symbol optional symbol; if null or blank, all symbols are returned
     * @param excludedOwner owner to exclude
     * @return list of pending sell offers
     */
    List<TradeOrder> findPendingSellOffers(String symbol, String excludedOwner);

    /**
     * Gets an owner's pending sell offers for a symbol.
     * <p>
     * Used to subtract shares already committed to offers and prevent a seller
     * from publishing more shares than are available.
     *
     * @param owner owner
     * @param symbol symbol
     * @return owner's pending offer list
     */
    List<TradeOrder> findPendingSellOffersByOwnerAndSymbol(String owner, String symbol);
}
