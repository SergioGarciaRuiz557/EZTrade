package com.trading.platform.eztrade.market.domain;

import java.util.Locale;

/**
 * Value object that represents a financial instrument symbol (ticker).
 * <p>
 * Validates that the ticker is not blank and matches a simple format:
 * letters/numbers with support for '.', '-' and '_' up to 20 characters.
 */
public record Symbol(String value) {

    /**
     * Compact constructor that applies domain validations to the symbol value.
     *
     * @throws InvalidSymbolException if the ticker is null, blank, or does not match the expected pattern
     */
    public Symbol {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidSymbolException("Ticker cannot be empty");
        }

        value = value.trim().toUpperCase(Locale.ROOT);

        if (!value.matches("^[A-Z0-9._-]{1,20}$")) {
            throw new InvalidSymbolException("Invalid ticker: " + value);
        }
    }

    /**
     * Static factory for creating a new {@link Symbol} from a text value.
     */
    public static Symbol of(String value) {
        return new Symbol(value);
    }
}
