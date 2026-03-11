package com.trading.platform.eztrade.trading.domain;

import java.math.BigDecimal;

/**
 * Value object para representar importes monetarios en el dominio.
 * <p>
 * Se utiliza tanto para precio unitario como para monto total.
 *
 * @param value importe monetario
 */
public record Money(BigDecimal value) {

    /**
     * Constructor compacto con validacion de dominio.
     *
     * @throws TradingDomainException si el importe es nulo o menor/igual a cero
     */
    public Money {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TradingDomainException("Money value must be greater than zero");
        }
    }

    /**
     * Multiplica el importe por una cantidad para obtener un total.
     *
     * @param quantity cantidad de activos
     * @return monto resultante de la multiplicacion
     */
    public Money multiply(Quantity quantity) {
        return new Money(value.multiply(quantity.value()));
    }
}
