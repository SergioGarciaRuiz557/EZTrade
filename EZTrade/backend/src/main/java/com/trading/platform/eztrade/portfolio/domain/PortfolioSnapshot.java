package com.trading.platform.eztrade.portfolio.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Vista de dominio de la cartera de un usuario.
 * <p>
 * Agrupa cash proyectado desde wallet, posiciones abiertas, coste base, PnL
 * realizado y, en consultas REST, valoraciones de mercado por simbolo.
 */
public record PortfolioSnapshot(
        String owner,
        BigDecimal cashAvailable,
        BigDecimal totalCostBasis,
        BigDecimal totalRealizedPnl,
        List<Position> positions,
        Map<String, PositionMarketValuation> marketValuations
) {

    public PortfolioSnapshot(String owner,
                             BigDecimal cashAvailable,
                             BigDecimal totalCostBasis,
                             BigDecimal totalRealizedPnl,
                             List<Position> positions) {
        this(owner, cashAvailable, totalCostBasis, totalRealizedPnl, positions, Map.of());
    }

    public PortfolioSnapshot {
        // Copias defensivas: el snapshot debe ser una foto estable de lectura.
        positions = List.copyOf(positions);
        marketValuations = Map.copyOf(marketValuations);
    }

    /**
     * Busca la valoracion de mercado por simbolo normalizando a mayusculas.
     */
    public Optional<PositionMarketValuation> marketValuationFor(String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(marketValuations.get(symbol.toUpperCase(Locale.ROOT)));
    }
}

