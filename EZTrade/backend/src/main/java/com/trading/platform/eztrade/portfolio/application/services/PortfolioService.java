package com.trading.platform.eztrade.portfolio.application.services;

import com.trading.platform.eztrade.portfolio.application.ports.in.GetPortfolioUseCase;
import com.trading.platform.eztrade.portfolio.application.ports.in.HandleOrderExecutedUseCase;
import com.trading.platform.eztrade.portfolio.application.ports.out.CashAccountRepositoryPort;
import com.trading.platform.eztrade.portfolio.application.ports.out.DomainEventPublisherPort;
import com.trading.platform.eztrade.portfolio.application.ports.out.PositionRepositoryPort;
import com.trading.platform.eztrade.portfolio.domain.CashAccount;
import com.trading.platform.eztrade.portfolio.domain.PortfolioDomainException;
import com.trading.platform.eztrade.portfolio.domain.PortfolioSnapshot;
import com.trading.platform.eztrade.portfolio.domain.Position;
import com.trading.platform.eztrade.portfolio.domain.events.PortfolioValuationUpdatedEvent;
import com.trading.platform.eztrade.portfolio.domain.events.PositionClosedEvent;
import com.trading.platform.eztrade.portfolio.domain.events.PositionIncreasedEvent;
import com.trading.platform.eztrade.portfolio.domain.events.PositionOpenedEvent;
import com.trading.platform.eztrade.portfolio.domain.events.PositionReducedEvent;
import com.trading.platform.eztrade.trading.domain.events.OrderExecutedEvent;
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
public class PortfolioService implements HandleOrderExecutedUseCase, GetPortfolioUseCase {

    private final PositionRepositoryPort positionRepository;
    private final CashAccountRepositoryPort cashAccountRepository;
    private final DomainEventPublisherPort eventPublisher;

    public PortfolioService(PositionRepositoryPort positionRepository,
                            CashAccountRepositoryPort cashAccountRepository,
                            DomainEventPublisherPort eventPublisher) {
        this.positionRepository = positionRepository;
        this.cashAccountRepository = cashAccountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void handle(OrderExecutedEvent event) {
        String owner = event.owner();
        String symbol = normalizeSymbol(event.symbol());
        BigDecimal quantity = positive(event.quantity(), "Quantity");
        BigDecimal price = positive(event.price(), "Price");
        BigDecimal grossAmount = quantity.multiply(price);

        CashAccount cashAccount = cashAccountRepository.findByOwner(owner).orElseGet(() -> CashAccount.open(owner));

        Side side = parseSide(event.side());
        switch (side) {
            case BUY -> handleBuy(owner, symbol, quantity, price, grossAmount, cashAccount);
            case SELL -> handleSell(owner, symbol, quantity, price, grossAmount, cashAccount);
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

        BigDecimal cashAvailable = cashAccountRepository.findByOwner(owner)
                .map(CashAccount::availableCash)
                .orElse(BigDecimal.ZERO);

        return new PortfolioSnapshot(owner, cashAvailable, totalCostBasis, totalRealizedPnl, positions);
    }

    private void handleBuy(String owner,
                           String symbol,
                           BigDecimal quantity,
                           BigDecimal price,
                           BigDecimal grossAmount,
                           CashAccount cashAccount) {
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

        cashAccountRepository.save(cashAccount.debit(grossAmount));
    }

    private void handleSell(String owner,
                            String symbol,
                            BigDecimal quantity,
                            BigDecimal price,
                            BigDecimal grossAmount,
                            CashAccount cashAccount) {
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

        cashAccountRepository.save(cashAccount.credit(grossAmount));
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

