package com.trading.platform.eztrade.trading.application.ports.in;

import com.trading.platform.eztrade.trading.domain.TradeOrder;

import java.util.List;

/**
 * Caso de uso para listar ofertas SELL pendientes que puede comprar un usuario.
 * <p>
 * La lista excluye las ofertas del propio comprador para impedir que se compre
 * a si mismo y, si se filtra por simbolo, valida que el precio no supere el
 * precio actual de mercado.
 */
public interface GetSellOffersUseCase {

    /**
     * Obtiene ofertas de venta disponibles.
     *
     * @param buyer usuario que consulta el marketplace
     * @param symbol simbolo opcional para filtrar ofertas
     * @return ordenes SELL pendientes disponibles para ese comprador
     */
    List<TradeOrder> getAvailableSellOffers(String buyer, String symbol);
}
