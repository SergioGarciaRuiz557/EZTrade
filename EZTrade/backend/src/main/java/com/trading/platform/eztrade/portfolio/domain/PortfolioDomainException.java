package com.trading.platform.eztrade.portfolio.domain;

/**
 * Excepcion de dominio para reglas de negocio del modulo portfolio.
 */
public class PortfolioDomainException extends RuntimeException {

    public PortfolioDomainException(String message) {
        super(message);
    }
}

