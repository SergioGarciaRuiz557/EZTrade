package com.trading.platform.eztrade.market.domain;

import java.util.Locale;

/**
 * Value object que representa el símbolo (ticker) de un instrumento financiero.
 * <p>
 * Se valida que el ticker no sea vacío y que cumpla un formato sencillo:
 * letras/números con soporte de '.', '-' y '_' con una longitud máxima de 20 caracteres.
 */
public record Symbol(String value) {

    /**
     * Constructor compacto que aplica validaciones de dominio sobre el valor del símbolo.
     *
     * @throws InvalidSymbolException si el ticker es nulo, vacío o no cumple el patrón esperado.
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
     * Fábrica estática para crear un nuevo {@link Symbol} a partir de un valor de texto.
     */
    public static Symbol of(String value) {
        return new Symbol(value);
    }
}
