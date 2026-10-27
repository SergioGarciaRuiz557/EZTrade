package com.trading.platform.eztrade.market.domain;

/**
 * Domain exception representing errors when communicating with market-related
 * external APIs (for example, data providers such as Alpha Vantage).
 * <p>
 * Used to encapsulate both network errors and invalid or unexpected responses
 * from the external provider.
 */
public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
