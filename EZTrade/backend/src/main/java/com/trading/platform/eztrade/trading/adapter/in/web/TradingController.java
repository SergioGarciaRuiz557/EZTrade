package com.trading.platform.eztrade.trading.adapter.in.web;

import com.trading.platform.eztrade.trading.adapter.in.web.dto.PlaceOrderRequest;
import com.trading.platform.eztrade.trading.adapter.in.web.dto.TradeOrderResponse;
import com.trading.platform.eztrade.trading.application.ports.in.CancelOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.ExecuteOrderUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.GetOrdersUseCase;
import com.trading.platform.eztrade.trading.application.ports.in.PlaceOrderUseCase;
import com.trading.platform.eztrade.trading.domain.OrderId;
import com.trading.platform.eztrade.trading.domain.TradeOrder;
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
 * Adaptador de entrada REST del modulo trading.
 * <p>
 * Expone operaciones para crear, ejecutar, cancelar y consultar ordenes
 * del usuario autenticado.
 */
@RestController
@RequestMapping("/api/v1/trading/orders")
public class TradingController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final ExecuteOrderUseCase executeOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetOrdersUseCase getOrdersUseCase;

    /**
     * Constructor con inyeccion de casos de uso de aplicacion.
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
     * Crea una nueva orden para el usuario autenticado.
     *
     * @param request datos de la orden
     * @param authentication contexto de autenticacion
     * @return orden creada
     */
    @PostMapping
    public ResponseEntity<TradeOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request,
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
     * Ejecuta una orden por su id.
     *
     * @param orderId identificador de la orden
     * @return orden ejecutada
     */
    @PostMapping("/{orderId}/execute")
    public ResponseEntity<TradeOrderResponse> execute(@PathVariable Long orderId) {
        TradeOrder order = executeOrderUseCase.execute(new OrderId(orderId));
        return ResponseEntity.ok(TradeOrderResponse.from(order));
    }

    /**
     * Cancela una orden por su id para el usuario autenticado.
     *
     * @param orderId identificador de la orden
     * @param authentication contexto de autenticacion
     * @return orden cancelada
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<TradeOrderResponse> cancel(@PathVariable Long orderId,
                                                     Authentication authentication) {
        TradeOrder order = cancelOrderUseCase.cancel(new OrderId(orderId), authentication.getName());
        return ResponseEntity.ok(TradeOrderResponse.from(order));
    }

    /**
     * Obtiene una orden por su id.
     *
     * @param orderId identificador de la orden
     * @return orden encontrada
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<TradeOrderResponse> getById(@PathVariable Long orderId) {
        return ResponseEntity.ok(TradeOrderResponse.from(getOrdersUseCase.getById(new OrderId(orderId))));
    }

    /**
     * Lista las ordenes del usuario autenticado.
     *
     * @param authentication contexto de autenticacion
     * @return lista de ordenes del propietario
     */
    @GetMapping
    public ResponseEntity<List<TradeOrderResponse>> getMine(Authentication authentication) {
        List<TradeOrderResponse> data = getOrdersUseCase.getByOwner(authentication.getName())
                .stream()
                .map(TradeOrderResponse::from)
                .toList();
        return ResponseEntity.ok(data);
    }
}

