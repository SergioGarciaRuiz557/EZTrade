package com.trading.platform.eztrade.trading.application.services;

import com.trading.platform.eztrade.market.api.MarketPriceLookupPort;
import com.trading.platform.eztrade.trading.application.ports.in.BuyFromMarketUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.BuySellOfferUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.CancelOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.ExecuteOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.GetOrdersUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.GetSellOffersUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.PlaceOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.PlaceSellOfferUseCase;
import com.trading.platform.eztrade.trading.application.ports.out.DomainEventPublisherPort;
import com.trading.platform.eztrade.trading.application.ports.out.TradeOrderRepositoryPort;
import com.trading.platform.eztrade.trading.domain.Money;
import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.OrderSide;
import com.trading.platform.eztrade.trading.domain.OrderStatus;
import com.trading.platform.eztrade.trading.domain.Quantity;
import com.trading.platform.eztrade.trading.domain.TradeOrder;
import com.trading.platform.eztrade.trading.domain.TradingDomainException;
import com.trading.platform.eztrade.trading.domain.events.OrderCancelledEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutionRequestEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderPlacedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * Application service for the trading module.
 * <p>
 * Implements the context's main use cases: creation, execution, cancellation,
 * and order queries.
 * <p>
 * Also centralizes the marketplace flow:
 * <ul>
 *   <li>direct market purchase using the current market/Alpha Vantage price,</li>
 *   <li>publication of user SELL offers,</li>
 *   <li>purchase of SELL offers by other users.</li>
 * </ul>
 * <p>
 * Architectural role:
 * <ul>
 *   <li>Orchestrates the domain.</li>
 *   <li>Persists through {@link TradeOrderRepositoryPort}.</li>
 *   <li>Performs inter-module communication through {@link DomainEventPublisherPort}.</li>
 * </ul>
 */
@Service
@Transactional
public class TradingService implements PlaceOrderUseCase,
        ExecuteOrderUseCase,
        CancelOrderUseCase,
        GetOrdersUseCase,
        BuyFromMarketUseCase,
        PlaceSellOfferUseCase,
        GetSellOffersUseCase,
        BuySellOfferUseCase {

    private final TradeOrderRepositoryPort tradeOrderRepositoryPort;
    private final DomainEventPublisherPort domainEventPublisherPort;
    private final MarketPriceLookupPort marketPriceLookupPort;

    /**
     * Creates the application service with its output ports.
     *
     * @param tradeOrderRepositoryPort order repository port
     * @param domainEventPublisherPort event publishing port
     * @param marketPriceLookupPort public market API for querying current prices
     */
    public TradingService(TradeOrderRepositoryPort tradeOrderRepositoryPort,
                          DomainEventPublisherPort domainEventPublisherPort,
                          MarketPriceLookupPort marketPriceLookupPort) {
        this.tradeOrderRepositoryPort = tradeOrderRepositoryPort;
        this.domainEventPublisherPort = domainEventPublisherPort;
        this.marketPriceLookupPort = marketPriceLookupPort;
    }

    /**
     * Registers a new order and publishes {@link OrderPlacedEvent}.
     *
     * @param command order creation data
     * @return created and persisted order
     */
    @Override
    public TradeOrder place(PlaceOrderCommand command) {
        // SELL orders represent offers published by users. Before persisting
        // them, marketplace rules are validated: maximum price and available shares.
        if (command.side() == OrderSide.SELL) {
            validateSellOffer(command.owner(), command.symbol(), command.quantity(), command.price());
        }

        TradeOrder order = TradeOrder.place(
                command.owner(),
                command.symbol(),
                command.side(),
                new Quantity(command.quantity()),
                new Money(command.price())
        );

        TradeOrder saved = tradeOrderRepositoryPort.save(order);

        try {
            domainEventPublisherPort.publish(new OrderPlacedEvent(
                    saved.id().value(),
                    saved.owner(),
                    saved.symbol(),
                    saved.side().name(),
                    saved.quantity().value(),
                    saved.price().value(),
                    LocalDateTime.now()
            ));
        } catch (RuntimeException ex) {
            if (isWalletInsufficientFunds(ex)) {
                throw new TradingDomainException(ex.getMessage());
            }
            throw ex;
        }

        return saved;
    }

    /**
     * Direct market purchase at the current price.
     * <p>
     * Creates a BUY order with the price queried from market and executes it in
     * the same flow so wallet reserves/settles funds and portfolio opens or
     * increases the position through events.
     */
    @Override
    public TradeOrder buy(BuyFromMarketCommand command) {
        String symbol = normalizeSymbol(command.symbol());
        BigDecimal currentPrice = currentMarketPrice(symbol);

        TradeOrder placed = place(new PlaceOrderCommand(
                command.owner(),
                symbol,
                OrderSide.BUY,
                command.quantity(),
                currentPrice
        ));

        return execute(placed.id());
    }

    /**
     * Publishes a SELL offer for other users.
     * <p>
     * Reuses {@link #place(PlaceOrderCommand)} to keep a single order-creation
     * path and apply the same domain validations.
     */
    @Override
    public TradeOrder placeSellOffer(PlaceSellOfferCommand command) {
        return place(new PlaceOrderCommand(
                command.owner(),
                command.symbol(),
                OrderSide.SELL,
                command.quantity(),
                command.price()
        ));
    }

    /**
     * Returns pending SELL offers available to the buyer.
     * <p>
     * If filtered by symbol, the current price is checked again so offers that
     * have moved above the allowed maximum are not shown.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TradeOrder> getAvailableSellOffers(String buyer, String symbol) {
        String normalizedBuyer = validateOwner(buyer);
        String normalizedSymbol = symbol == null || symbol.isBlank() ? null : normalizeSymbol(symbol);
        List<TradeOrder> offers = tradeOrderRepositoryPort.findPendingSellOffers(normalizedSymbol, normalizedBuyer);

        if (normalizedSymbol == null) {
            return offers;
        }

        BigDecimal currentPrice = currentMarketPrice(normalizedSymbol);
        return offers.stream()
                .filter(offer -> offer.price().value().compareTo(currentPrice) <= 0)
                .toList();
    }

    /**
     * Buys another user's sell offer.
     * <p>
     * The method locks the offer with {@code findByIdForUpdate} to reduce the
     * risk of two buyers trying to execute the same offer at once. It then
     * creates a BUY for the buyer and executes both the BUY and the original
     * SELL so wallet and portfolio apply their movements through events.
     */
    @Override
    public MarketplaceTradeResult buyOffer(BuySellOfferCommand command) {
        String buyer = validateOwner(command.buyer());
        OrderId sellOfferId = new OrderId(command.sellOfferId());
        TradeOrder sellOffer = tradeOrderRepositoryPort.findByIdForUpdate(sellOfferId)
                .orElseThrow(() -> new TradingDomainException("Oferta de venta no encontrada: " + sellOfferId.value()));

        validateOfferCanBeBought(sellOffer, buyer);
        validateCurrentPriceLimit(sellOffer.symbol(), sellOffer.price().value());
        validateSellerCanCoverPendingOffers(sellOffer.owner(), sellOffer.symbol());

        TradeOrder buyerOrder = place(new PlaceOrderCommand(
                buyer,
                sellOffer.symbol(),
                OrderSide.BUY,
                sellOffer.quantity().value(),
                sellOffer.price().value()
        ));

        TradeOrder executedBuyerOrder = execute(Objects.requireNonNull(
                buyerOrder.id(),
                "El id de la orden del comprador es obligatorio"
        ));
        TradeOrder executedSellerOrder = execute(sellOffer.id());

        return new MarketplaceTradeResult(executedBuyerOrder, executedSellerOrder);
    }

    /**
     * Executes an existing order and publishes {@link OrderExecutionRequestEvent}.
     *
     * @param orderId order identifier
     * @return executed order
     * @throws TradingDomainException if the order does not exist or cannot be executed
     */
    @Override
    public TradeOrder execute(OrderId orderId) {
        TradeOrder current = tradeOrderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new TradingDomainException("Orden no encontrada: " + orderId.value()));

        TradeOrder executed = tradeOrderRepositoryPort.save(current.execute());
        OrderExecutionRequestEvent event = new OrderExecutionRequestEvent(
                executed.id().value(),
                executed.owner(),
                executed.symbol(),
                executed.side().name(),
                executed.quantity().value(),
                executed.price().value(),
                LocalDateTime.now());
        try {
            // The actual money execution does not happen in trading. Wallet
            // listens to this event, validates funds, and, if everything is OK,
            // later publishes the OrderExecutedEvent consumed by portfolio.
            domainEventPublisherPort.publish(event);
        } catch (RuntimeException ex) {
            if (isWalletInsufficientFunds(ex)) {
                throw new TradingDomainException(ex.getMessage());
            }
            throw ex;
        }


        return executed;
    }

    /**
     * Cancels an existing order and publishes {@link OrderCancelledEvent}.
     *
     * @param orderId order identifier
     * @param requestedBy user requesting cancellation
     * @return cancelled order
     * @throws TradingDomainException if the order does not exist or cannot be cancelled
     */
    @Override
    public TradeOrder cancel(OrderId orderId, String requestedBy) {
        TradeOrder current = tradeOrderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new TradingDomainException("Orden no encontrada: " + orderId.value()));

        TradeOrder cancelled = tradeOrderRepositoryPort.save(current.cancel(requestedBy));

        domainEventPublisherPort.publish(new OrderCancelledEvent(
                cancelled.id().value(),
                cancelled.owner(),
                cancelled.symbol(),
                LocalDateTime.now()
        ));

        return cancelled;
    }

    /**
     * Queries an order by id.
     *
     * @param orderId order identifier
     * @return found order
     * @throws TradingDomainException if it does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public TradeOrder getById(OrderId orderId) {
        return tradeOrderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new TradingDomainException("Orden no encontrada: " + orderId.value()));
    }

    /**
     * Queries all orders for an owner.
     *
     * @param owner order owner
     * @return order list
     */
    @Override
    @Transactional(readOnly = true)
    public List<TradeOrder> getByOwner(String owner) {
        return tradeOrderRepositoryPort.findByOwner(owner);
    }

    /**
     * Validates a SELL offer before saving it.
     * <p>
     * This rule prevents publishing nonexistent shares and prices above the
     * current Alpha Vantage price.
     */
    private void validateSellOffer(String owner, String symbol, BigDecimal quantity, BigDecimal price) {
        String validatedOwner = validateOwner(owner);
        String normalizedSymbol = normalizeSymbol(symbol);
        BigDecimal requestedQuantity = positive(quantity, "Cantidad");
        BigDecimal requestedPrice = positive(price, "Precio");

        validateCurrentPriceLimit(normalizedSymbol, requestedPrice);
        validateAvailablePositionForNewOffer(validatedOwner, normalizedSymbol, requestedQuantity);
    }

    /**
     * Checks that the offered price does not exceed the current market price.
     */
    private void validateCurrentPriceLimit(String symbol, BigDecimal requestedPrice) {
        BigDecimal currentPrice = currentMarketPrice(symbol);
        if (requestedPrice.compareTo(currentPrice) > 0) {
            throw new TradingDomainException(
                    "El precio de venta no puede superar el precio actual de mercado de " + symbol
                            + ". Precio actual: " + currentPrice
            );
        }
    }

    /**
     * Checks that the seller has enough free shares for a new offer. Free shares
     * are bought and unsold shares minus those already committed in other pending offers.
     */
    private void validateAvailablePositionForNewOffer(String owner, String symbol, BigDecimal quantityToOffer) {
        BigDecimal ownedQuantity = ownedQuantity(owner, symbol);
        BigDecimal alreadyOfferedQuantity = pendingSellOfferQuantity(owner, symbol);
        BigDecimal availableToOffer = ownedQuantity.subtract(alreadyOfferedQuantity);

        if (quantityToOffer.compareTo(availableToOffer) > 0) {
            throw new TradingDomainException(
                    "No puedes vender mas acciones que las disponibles para " + symbol
                            + ". Disponibles para ofertar: " + availableToOffer
            );
        }
    }

    /**
     * Revalidates that the seller still has enough shares right before executing
     * an offer. This is an additional defense in case their position changed
     * since publishing the offer.
     */
    private void validateSellerCanCoverPendingOffers(String owner, String symbol) {
        BigDecimal ownedQuantity = ownedQuantity(owner, symbol);
        BigDecimal pendingQuantity = pendingSellOfferQuantity(owner, symbol);

        if (pendingQuantity.compareTo(ownedQuantity) > 0) {
            throw new TradingDomainException(
                    "El vendedor ya no tiene acciones suficientes para cubrir sus ofertas pendientes de " + symbol
            );
        }
    }

    /**
     * Calculates the user's net share quantity using the executed-order history
     * from the trading module itself.
     * <p>
     * This avoids a direct dependency from trading to portfolio: portfolio already
     * depends on trading events, and querying portfolio from trading would create
     * a module cycle in Spring Modulith.
     */
    private BigDecimal ownedQuantity(String owner, String symbol) {
        BigDecimal owned = tradeOrderRepositoryPort.findExecutedOrdersByOwnerAndSymbol(owner, symbol)
                .stream()
                .map(order -> order.side() == OrderSide.BUY
                        ? order.quantity().value()
                        : order.quantity().value().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (owned.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TradingDomainException("No existe posicion para el simbolo: " + symbol);
        }
        return owned;
    }

    /**
     * Sums the shares the seller already has reserved in pending SELL offers so
     * they cannot publish more shares than are available.
     */
    private BigDecimal pendingSellOfferQuantity(String owner, String symbol) {
        return tradeOrderRepositoryPort.findPendingSellOffersByOwnerAndSymbol(owner, symbol)
                .stream()
                .map(order -> order.quantity().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Validates that the received order is an offer the user can buy.
     */
    private void validateOfferCanBeBought(TradeOrder sellOffer, String buyer) {
        if (sellOffer.side() != OrderSide.SELL) {
            throw new TradingDomainException("La orden no es una oferta de venta: " + sellOffer.id().value());
        }
        if (sellOffer.status() != OrderStatus.PENDING) {
            throw new TradingDomainException("Solo se pueden comprar ofertas de venta pendientes");
        }
        if (sellOffer.owner().equalsIgnoreCase(buyer)) {
            throw new TradingDomainException("No puedes comprar tu propia oferta de venta");
        }
    }

    /**
     * Obtains the current price from market and translates any external failure
     * into a trading domain exception with an API-friendly message.
     */
    private BigDecimal currentMarketPrice(String symbol) {
        try {
            BigDecimal currentPrice = marketPriceLookupPort.currentPrice(symbol);
            return positive(currentPrice, "Precio actual de mercado");
        } catch (TradingDomainException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new TradingDomainException("No se puede validar el precio actual de mercado de " + symbol + ": " + ex.getMessage());
        }
    }

    private static String validateOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new TradingDomainException("El propietario es obligatorio");
        }
        return owner;
    }

    private static String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new TradingDomainException("El simbolo es obligatorio");
        }
        return symbol.trim().toUpperCase(Locale.ROOT);
    }

    private static BigDecimal positive(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TradingDomainException(field + " debe ser mayor que cero");
        }
        return value;
    }

    private static boolean isWalletInsufficientFunds(RuntimeException ex) {
        Throwable cursor = ex;
        while (cursor != null) {
            String message = cursor.getMessage();
            if (message != null && message.contains("Insufficient wallet funds")) {
                return true;
            }
            cursor = cursor.getCause();
        }
        return false;
    }
}
