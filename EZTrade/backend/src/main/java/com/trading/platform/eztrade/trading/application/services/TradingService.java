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
 * Servicio de aplicacion del modulo trading.
 * <p>
 * Implementa los casos de uso principales del contexto:
 * alta, ejecucion, cancelacion y consulta de ordenes.
 * <p>
 * Tambien concentra el nuevo flujo de marketplace:
 * <ul>
 *   <li>compra directa al mercado usando el precio actual de market/AlphaVantage,</li>
 *   <li>publicacion de ofertas SELL de usuarios,</li>
 *   <li>compra de ofertas SELL por otros usuarios.</li>
 * </ul>
 * <p>
 * Rol arquitectonico:
 * <ul>
 *   <li>Orquesta el dominio.</li>
 *   <li>Persistencia via {@link TradeOrderRepositoryPort}.</li>
 *   <li>Comunicacion inter-modulo via {@link DomainEventPublisherPort}.</li>
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
     * Crea el servicio de aplicacion con sus puertos de salida.
     *
     * @param tradeOrderRepositoryPort puerto de repositorio de ordenes
     * @param domainEventPublisherPort puerto para publicacion de eventos
     * @param marketPriceLookupPort API publica de market para consultar precios actuales
     */
    public TradingService(TradeOrderRepositoryPort tradeOrderRepositoryPort,
                          DomainEventPublisherPort domainEventPublisherPort,
                          MarketPriceLookupPort marketPriceLookupPort) {
        this.tradeOrderRepositoryPort = tradeOrderRepositoryPort;
        this.domainEventPublisherPort = domainEventPublisherPort;
        this.marketPriceLookupPort = marketPriceLookupPort;
    }

    /**
     * Registra una nueva orden y publica {@link OrderPlacedEvent}.
     *
     * @param command datos de creacion de la orden
     * @return orden creada y persistida
     */
    @Override
    public TradeOrder place(PlaceOrderCommand command) {
        // Las SELL representan ofertas publicadas por usuarios. Antes de
        // persistirlas se validan las reglas de marketplace: precio maximo y
        // acciones disponibles.
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

        domainEventPublisherPort.publish(new OrderPlacedEvent(
                saved.id().value(),
                saved.owner(),
                saved.symbol(),
                saved.side().name(),
                saved.quantity().value(),
                saved.price().value(),
                LocalDateTime.now()
        ));

        return saved;
    }

    /**
     * Compra directa al mercado al precio actual.
     * <p>
     * Crea una orden BUY con el precio consultado a market y la ejecuta en el
     * mismo flujo para que wallet reserve/liquide fondos y portfolio abra o
     * incremente la posicion mediante eventos.
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
     * Publica una oferta SELL para otros usuarios.
     * <p>
     * Reutiliza {@link #place(PlaceOrderCommand)} para mantener una unica ruta
     * de creacion de ordenes y aplicar las mismas validaciones de dominio.
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
     * Devuelve ofertas SELL pendientes disponibles para el comprador.
     * <p>
     * Si se filtra por simbolo, se comprueba de nuevo el precio actual para no
     * mostrar ofertas que hayan quedado por encima del maximo permitido.
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
     * Compra una oferta de venta de otro usuario.
     * <p>
     * El metodo bloquea la oferta con {@code findByIdForUpdate} para reducir el
     * riesgo de que dos compradores intenten ejecutar la misma oferta a la vez.
     * Despues crea una BUY para el comprador y ejecuta tanto la BUY como la SELL
     * original para que wallet y portfolio apliquen sus movimientos por eventos.
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
     * Ejecuta una orden existente y publica {@link OrderExecutionRequestEvent}.
     *
     * @param orderId identificador de la orden
     * @return orden ejecutada
     * @throws TradingDomainException si la orden no existe o no puede ejecutarse
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
            // La ejecucion real del dinero no ocurre en trading. Wallet escucha
            // este evento, valida fondos y, si todo va bien, publica despues el
            // OrderExecutedEvent que consume portfolio.
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
     * Cancela una orden existente y publica {@link OrderCancelledEvent}.
     *
     * @param orderId identificador de la orden
     * @param requestedBy usuario que solicita la cancelacion
     * @return orden cancelada
     * @throws TradingDomainException si la orden no existe o no puede cancelarse
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
     * Consulta una orden por id.
     *
     * @param orderId identificador de la orden
     * @return orden encontrada
     * @throws TradingDomainException si no existe
     */
    @Override
    @Transactional(readOnly = true)
    public TradeOrder getById(OrderId orderId) {
        return tradeOrderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new TradingDomainException("Orden no encontrada: " + orderId.value()));
    }

    /**
     * Consulta todas las ordenes de un propietario.
     *
     * @param owner propietario de las ordenes
     * @return lista de ordenes
     */
    @Override
    @Transactional(readOnly = true)
    public List<TradeOrder> getByOwner(String owner) {
        return tradeOrderRepositoryPort.findByOwner(owner);
    }

    /**
     * Valida una oferta SELL antes de guardarla.
     * <p>
     * Esta regla evita publicar acciones inexistentes y evita precios superiores
     * al precio actual de AlphaVantage.
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
     * Comprueba que el precio ofertado no supere el precio actual de mercado.
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
     * Comprueba que el vendedor tenga acciones libres suficientes para una nueva
     * oferta. Las acciones libres son las compradas y no vendidas menos las que
     * ya estan comprometidas en otras ofertas pendientes.
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
     * Revalida que el vendedor siga teniendo acciones suficientes justo antes de
     * ejecutar una oferta. Es una defensa adicional por si su posicion cambio
     * desde que publico la oferta.
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
     * Calcula la cantidad neta de acciones del usuario usando el historico de
     * ordenes ejecutadas del propio modulo trading.
     * <p>
     * Se hace asi para evitar una dependencia directa de trading hacia portfolio:
     * portfolio ya depende de los eventos de trading, y consultar portfolio desde
     * trading crearia un ciclo de modulos en Spring Modulith.
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
     * Suma las acciones que el vendedor ya tiene reservadas en ofertas SELL
     * pendientes para no permitir publicar mas acciones de las disponibles.
     */
    private BigDecimal pendingSellOfferQuantity(String owner, String symbol) {
        return tradeOrderRepositoryPort.findPendingSellOffersByOwnerAndSymbol(owner, symbol)
                .stream()
                .map(order -> order.quantity().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Valida que la orden recibida sea una oferta comprable por el usuario.
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
     * Obtiene el precio actual desde market y traduce cualquier fallo externo a
     * una excepcion de dominio de trading con mensaje comprensible para la API.
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
