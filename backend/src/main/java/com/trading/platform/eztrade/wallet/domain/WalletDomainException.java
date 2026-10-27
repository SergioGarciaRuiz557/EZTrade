package com.trading.platform.eztrade.wallet.domain;

/**
 * Domain exception for the Wallet module.
 * <p>
 * Thrown when a business rule or model invariant is violated (for example,
 * trying to withdraw more than the available balance, reserving funds without
 * enough balance, null/negative amounts, blank owners, etc.).
 * <p>
 * It is a {@link RuntimeException} because it represents a domain usage error
 * within the same process and is expected to be translated by the application
 * layer when necessary (for example, to an API error).
 */
public class WalletDomainException extends RuntimeException {

    public WalletDomainException(String message) {
        super(message);
    }
}

