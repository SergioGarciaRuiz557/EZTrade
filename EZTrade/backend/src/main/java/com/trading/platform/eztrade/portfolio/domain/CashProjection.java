package com.trading.platform.eztrade.portfolio.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Proyeccion local del cash disponible del usuario, sincronizada desde eventos de wallet.
 */
public record CashProjection(
        String owner,
        BigDecimal availableCash,
        LocalDateTime updatedAt
) {
}

