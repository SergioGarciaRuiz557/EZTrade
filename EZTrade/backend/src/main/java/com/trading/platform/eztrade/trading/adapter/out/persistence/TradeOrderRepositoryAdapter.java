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

    @Override
    public Optional<TradeOrder> findByIdForUpdate(OrderId orderId) {
        // Delegamos en una consulta con bloqueo pesimista para proteger la
        // compra de ofertas frente a ejecuciones concurrentes.
        return repository.findByIdForUpdate(orderId.value()).map(TradeOrderMapper::toDomain);
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

    @Override
    public List<TradeOrder> findExecutedOrdersByOwnerAndSymbol(String owner, String symbol) {
        // Normalizamos el simbolo aqui porque los metodos derivados de Spring
        // Data comparan por igualdad exacta contra lo guardado en la base.
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
            // Sin simbolo se devuelven todas las ofertas pendientes de otros
            // usuarios. El servicio de aplicacion decide si necesita filtros
            // adicionales de negocio.
            offers = repository.findBySideAndStatusAndOwnerNot(OrderSide.SELL, OrderStatus.PENDING, excludedOwner);
        } else {
            // Con simbolo se reduce el resultado en base de datos para que el
            // marketplace no traiga ofertas innecesarias.
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
        // Consulta de soporte para calcular acciones ya comprometidas por un
        // vendedor en ofertas SELL pendientes.
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
