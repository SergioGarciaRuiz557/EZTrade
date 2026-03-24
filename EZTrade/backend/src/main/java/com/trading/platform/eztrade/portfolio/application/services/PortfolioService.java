package com.trading.platform.eztrade.portfolio.application.services;

import com.trading.platform.eztrade.portfolio.application.ports.in.GetPortfolioUseCase;
import com.trading.platform.eztrade.portfolio.application.ports.in.HandleOrderExecutedUseCase;
import com.trading.platform.eztrade.portfolio.application.ports.in.HandleWalletCashUpdatedUseCase;
import com.trading.platform.eztrade.portfolio.application.ports.out.CashProjectionRepositoryPort;
import com.trading.platform.eztrade.portfolio.application.ports.out.DomainEventPublisherPort;
import com.trading.platform.eztrade.portfolio.application.ports.out.PositionRepositoryPort;
import com.trading.platform.eztrade.portfolio.domain.CashProjection;
import com.trading.platform.eztrade.portfolio.domain.PortfolioDomainException;
import com.trading.platform.eztrade.portfolio.domain.PortfolioSnapshot;
import com.trading.platform.eztrade.portfolio.domain.Position;
import com.trading.platform.eztrade.portfolio.domain.events.PortfolioValuationUpdatedEvent;
import com.trading.platform.eztrade.portfolio.domain.events.PositionClosedEvent;
import com.trading.platform.eztrade.portfolio.domain.events.PositionIncreasedEvent;
import com.trading.platform.eztrade.portfolio.domain.events.PositionOpenedEvent;
import com.trading.platform.eztrade.portfolio.domain.events.PositionReducedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
import com.trading.platform.eztrade.wallet.domain.events.AvailableCashUpdatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Servicio de aplicacion del modulo portfolio.
 */
@Service
@Transactional
public class PortfolioService implements HandleOrderExecutedUseCase, GetPortfolioUseCase, HandleWalletCashUpdatedUseCase {

    private final PositionRepositoryPort positionRepository;
    private final CashProjectionRepositoryPort cashProjectionRepository;
    private final DomainEventPublisherPort eventPublisher;

    public PortfolioService(PositionRepositoryPort positionRepository,
                            CashProjectionRepositoryPort cashProjectionRepository,
                            DomainEventPublisherPort eventPublisher) {
        this.positionRepository = positionRepository;
        this.cashProjectionRepository = cashProjectionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void handle(OrderExecutedEvent event) {
        String owner = event.owner();
        String symbol = normalizeSymbol(event.symbol());
        BigDecimal quantity = positive(event.quantity(), "Quantity");
        BigDecimal price = positive(event.price(), "Price");

        Side side = parseSide(event.side());
        switch (side) {
            case BUY -> handleBuy(owner, symbol, quantity, price);
            case SELL -> handleSell(owner, symbol, quantity, price);
        }

        PortfolioSnapshot snapshot = getByOwner(owner);
        eventPublisher.publish(new PortfolioValuationUpdatedEvent(
                owner,
                snapshot.cashAvailable(),
                snapshot.totalCostBasis(),
                snapshot.totalRealizedPnl(),
                LocalDateTime.now()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioSnapshot getByOwner(String owner) {
        List<Position> positions = positionRepository.findByOwner(owner);
        BigDecimal totalCostBasis = positions.stream()
                .map(Position::investedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRealizedPnl = positions.stream()
                .map(Position::realizedPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cashAvailable = cashProjectionRepository.findByOwner(owner)
                .map(CashProjection::availableCash)
                .orElse(BigDecimal.ZERO);

        return new PortfolioSnapshot(owner, cashAvailable, totalCostBasis, totalRealizedPnl, positions);
    }

    @Override
    public void handle(AvailableCashUpdatedEvent event) {
        if (event.owner() == null || event.owner().isBlank()) {
            throw new PortfolioDomainException("Owner is required");
        }
        if (event.availableCash() == null) {
            throw new PortfolioDomainException("Available cash is required");
        }

        LocalDateTime updatedAt = event.occurredAt() == null ? LocalDateTime.now() : event.occurredAt();
        cashProjectionRepository.save(new CashProjection(event.owner(), event.availableCash(), updatedAt));
    }

    private void handleBuy(String owner,
                           String symbol,
                           BigDecimal quantity,
                           BigDecimal price) {
        Position current = positionRepository.findByOwnerAndSymbol(owner, symbol).orElse(null);
        Position saved;

        if (current == null) {
            saved = positionRepository.save(Position.open(owner, symbol, quantity, price));
            eventPublisher.publish(new PositionOpenedEvent(
                    owner,
                    symbol,
                    saved.quantity(),
                    saved.averageCost(),
                    LocalDateTime.now()
            ));
        } else {
            saved = positionRepository.save(current.increase(quantity, price));
            eventPublisher.publish(new PositionIncreasedEvent(
                    owner,
                    symbol,
                    saved.quantity(),
                    saved.averageCost(),
                    LocalDateTime.now()
            ));
        }
    }

    private void handleSell(String owner,
                            String symbol,
                            BigDecimal quantity,
                            BigDecimal price) {
        Position current = positionRepository.findByOwnerAndSymbol(owner, symbol)
                .orElseThrow(() -> new PortfolioDomainException("Position not found for symbol: " + symbol));

        Position.SellResult result = current.reduce(quantity, price);
        Position updated = result.position();

        if (updated.isClosed()) {
            positionRepository.deleteByOwnerAndSymbol(owner, symbol);
            eventPublisher.publish(new PositionClosedEvent(
                    owner,
                    symbol,
                    result.realizedPnlDelta(),
                    updated.realizedPnl(),
                    LocalDateTime.now()
            ));
        } else {
            positionRepository.save(updated);
            eventPublisher.publish(new PositionReducedEvent(
                    owner,
                    symbol,
                    updated.quantity(),
                    result.realizedPnlDelta(),
                    updated.realizedPnl(),
                    LocalDateTime.now()
            ));
        }
    }

    private static String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new PortfolioDomainException("Symbol is required");
        }
        return symbol.toUpperCase(Locale.ROOT);
    }

    private static BigDecimal positive(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PortfolioDomainException(field + " must be greater than zero");
        }
        return value;
    }

    private static Side parseSide(String sideValue) {
        if (sideValue == null || sideValue.isBlank()) {
            throw new PortfolioDomainException("Order side is required");
        }
        try {
            return Side.valueOf(sideValue.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new PortfolioDomainException("Unsupported order side: " + sideValue);
        }
    }

    private enum Side {
        BUY,
        SELL
    }
}

