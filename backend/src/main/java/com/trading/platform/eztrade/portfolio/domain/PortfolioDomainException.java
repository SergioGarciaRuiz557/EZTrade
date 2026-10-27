package com.trading.platform.eztrade.portfolio.domain;

/**
 * Domain exception for portfolio module business rules.
 */
public class PortfolioDomainException extends RuntimeException {

    public PortfolioDomainException(String message) {
        super(message);
    }
}

