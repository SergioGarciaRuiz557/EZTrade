package com.trading.platform.eztrade.trading.adapter.in.web;

import com.trading.platform.eztrade.trading.adapter.in.web.dto.PlaceOrderRequest;
import com.trading.platform.eztrade.trading.adapter.in.web.dto.TradeOrderResponse;
import com.trading.platform.eztrade.trading.application.ports.in.CancelOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.ExecuteOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.GetOrdersUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.PlaceOrderUseCase;
import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.OrderSide;
import com.trading.platform.eztrade.trading.domain.TradeOrder;
import com.trading.platform.eztrade.trading.domain.TradingDomainException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST input adapter for the trading module.
 * <p>
 * Exposes operations to create, execute, cancel, and query orders owned by the
 * authenticated user.
 */
@RestController
@RequestMapping("/api/v1/trading/orders")
public class TradingController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final ExecuteOrderUseCase executeOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetOrdersUseCase getOrdersUseCase;

    /**
     * Constructor with application use case injection.
     */
    public TradingController(PlaceOrderUseCase placeOrderUseCase,
                             ExecuteOrderUseCase executeOrderUseCase,
                             CancelOrderUseCase cancelOrderUseCase,
                             GetOrdersUseCase getOrdersUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.executeOrderUseCase = executeOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.getOrdersUseCase = getOrdersUseCase;
    }

    /**
     * Creates a new order for the authenticated user.
     *
     * @param request order data
     * @param authentication authentication context
     * @return created order
     */
    @PostMapping
    public ResponseEntity<TradeOrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request,
                                                         Authentication authentication) {
        TradeOrder order = placeOrderUseCase.place(new PlaceOrderUseCase.PlaceOrderCommand(
                authentication.getName(),
                request.symbol(),
                request.side(),
                request.quantity(),
                request.price()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(TradeOrderResponse.from(order));
    }

    /**
     * Executes an order by id.
     *
     * @param orderId order identifier
     * @return executed order
     */
    @PostMapping("/{orderId}/execute")
    public ResponseEntity<TradeOrderResponse> execute(@PathVariable Long orderId,
                                                      Authentication authentication) {
        TradeOrder existing = ensureOwnerOrForbidden(orderId, authentication);
        // A pending SELL is an offer published in the marketplace. If the
        // seller could execute it directly, wallet would credit cash without an
        // actual buyer. For that reason it can only be executed through
        // /offers/{offerId}/buy, where the buyer's BUY order is also created.
        if (existing.side() == OrderSide.SELL) {
            throw new TradingDomainException("Las ofertas de venta deben comprarse mediante /api/v1/trading/offers/{offerId}/buy");
        }
        TradeOrder order = executeOrderUseCase.execute(new OrderId(orderId));
        return ResponseEntity.ok(TradeOrderResponse.from(order));
    }

    /**
     * Cancels an order by id for the authenticated user.
     *
     * @param orderId order identifier
     * @param authentication authentication context
     * @return canceled order
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<TradeOrderResponse> cancel(@PathVariable Long orderId,
                                                     Authentication authentication) {
        TradeOrder order = cancelOrderUseCase.cancel(new OrderId(orderId), authentication.getName());
        return ResponseEntity.ok(TradeOrderResponse.from(order));
    }

    /**
     * Gets an order by id.
     *
     * @param orderId order identifier
     * @return found order
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<TradeOrderResponse> getById(@PathVariable Long orderId,
                                                      Authentication authentication) {
        TradeOrder order = ensureOwnerOrForbidden(orderId, authentication);
        return ResponseEntity.ok(TradeOrderResponse.from(order));
    }

    /**
     * Lists the authenticated user's orders.
     *
     * @param authentication authentication context
     * @return owner's order list
     */
    @GetMapping
    public ResponseEntity<List<TradeOrderResponse>> getMine(Authentication authentication) {
        List<TradeOrderResponse> data = getOrdersUseCase.getByOwner(authentication.getName())
                .stream()
                .map(TradeOrderResponse::from)
                .toList();
        return ResponseEntity.ok(data);
    }

    private TradeOrder ensureOwnerOrForbidden(Long orderId, Authentication authentication) {
        TradeOrder order = getOrdersUseCase.getById(new OrderId(orderId));
        if (!order.owner().equalsIgnoreCase(authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("No puedes acceder a la orden de otro usuario");
        }
        return order;
    }
}

