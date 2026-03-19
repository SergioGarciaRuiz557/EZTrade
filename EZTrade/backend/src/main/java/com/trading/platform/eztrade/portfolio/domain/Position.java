package com.trading.platform.eztrade.portfolio.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Agregado de dominio que representa una posicion de un usuario en un simbolo.
 */
public class Position {

    private final String owner;
    private final String symbol;
    private final BigDecimal quantity;
    private final BigDecimal averageCost;
    private final BigDecimal realizedPnl;
    private final LocalDateTime updatedAt;

    private Position(String owner,
                     String symbol,
                     BigDecimal quantity,
                     BigDecimal averageCost,
                     BigDecimal realizedPnl,
                     LocalDateTime updatedAt) {
        this.owner = validateOwner(owner);
        this.symbol = validateSymbol(symbol);
        this.quantity = requirePositiveOrZero(quantity, "Quantity");
        this.averageCost = requirePositiveOrZero(averageCost, "Average cost");
        this.realizedPnl = requireNotNull(realizedPnl, "Realized PnL is required");
        this.updatedAt = requireNotNull(updatedAt, "UpdatedAt is required");
    }

    public static Position open(String owner, String symbol, BigDecimal quantity, BigDecimal executionPrice) {
        requirePositive(quantity, "Quantity");
        requirePositive(executionPrice, "Execution price");
        return new Position(owner, symbol, quantity, executionPrice, BigDecimal.ZERO, LocalDateTime.now());
    }

    public static Position rehydrate(String owner,
                                     String symbol,
                                     BigDecimal quantity,
                                     BigDecimal averageCost,
                                     BigDecimal realizedPnl,
                                     LocalDateTime updatedAt) {
        return new Position(owner, symbol, quantity, averageCost, realizedPnl, updatedAt);
    }

    public Position increase(BigDecimal quantityToAdd, BigDecimal executionPrice) {
        requirePositive(quantityToAdd, "Quantity to add");
        requirePositive(executionPrice, "Execution price");

        BigDecimal currentCost = quantity.multiply(averageCost);
        BigDecimal addedCost = quantityToAdd.multiply(executionPrice);
        BigDecimal newQuantity = quantity.add(quantityToAdd);
        BigDecimal newAverageCost = currentCost.add(addedCost)
                .divide(newQuantity, 8, RoundingMode.HALF_UP);

        return new Position(owner, symbol, newQuantity, newAverageCost, realizedPnl, LocalDateTime.now());
    }

    public SellResult reduce(BigDecimal quantityToSell, BigDecimal executionPrice) {
        requirePositive(quantityToSell, "Quantity to sell");
        requirePositive(executionPrice, "Execution price");

        if (quantityToSell.compareTo(quantity) > 0) {
            throw new PortfolioDomainException("Cannot sell more units than current position");
        }

        BigDecimal realizedDelta = executionPrice.subtract(averageCost).multiply(quantityToSell);
        BigDecimal newRealizedPnl = realizedPnl.add(realizedDelta);
        BigDecimal remainingQuantity = quantity.subtract(quantityToSell);
        BigDecimal newAverageCost = remainingQuantity.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : averageCost;

        Position updated = new Position(
                owner,
                symbol,
                remainingQuantity,
                newAverageCost,
                newRealizedPnl,
                LocalDateTime.now()
        );

        return new SellResult(updated, realizedDelta);
    }

    public BigDecimal investedAmount() {
        return quantity.multiply(averageCost);
    }

    public boolean isClosed() {
        return quantity.compareTo(BigDecimal.ZERO) == 0;
    }

    public String owner() {
        return owner;
    }

    public String symbol() {
        return symbol;
    }

    public BigDecimal quantity() {
        return quantity;
    }

    public BigDecimal averageCost() {
        return averageCost;
    }

    public BigDecimal realizedPnl() {
        return realizedPnl;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
    }

    public record SellResult(Position position, BigDecimal realizedPnlDelta) {
    }

    private static String validateOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new PortfolioDomainException("Owner is required");
        }
        return owner;
    }

    private static String validateSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new PortfolioDomainException("Symbol is required");
        }
        return symbol.toUpperCase();
    }

    private static BigDecimal requirePositiveOrZero(BigDecimal value, String fieldName) {
        requireNotNull(value, fieldName + " is required");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new PortfolioDomainException(fieldName + " must be zero or greater");
        }
        return value;
    }

    private static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        requireNotNull(value, fieldName + " is required");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PortfolioDomainException(fieldName + " must be greater than zero");
        }
        return value;
    }

    private static <T> T requireNotNull(T value, String message) {
        return Objects.requireNonNull(value, message);
    }
}

