package com.trading.platform.eztrade.market.adapter.in.web;

import com.trading.platform.eztrade.market.application.ports.in.GetCurrentPricesUseCase;
import com.trading.platform.eztrade.market.application.ports.in.GetInstrumentsUseCase;
import com.trading.platform.eztrade.market.domain.Instrument;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para consultar instrumentos y precios de mercado.
 */
@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    private final GetInstrumentsUseCase getInstrumentsUseCase;
    private final GetCurrentPricesUseCase getCurrentPricesUseCase;

    public MarketController(GetInstrumentsUseCase getInstrumentsUseCase,
                            GetCurrentPricesUseCase getCurrentPricesUseCase) {
        this.getInstrumentsUseCase = getInstrumentsUseCase;
        this.getCurrentPricesUseCase = getCurrentPricesUseCase;
    }

    /**
     * Devuelve la lista de instrumentos financieros disponibles.
     */
    @GetMapping("/instruments")
    public ResponseEntity<List<Instrument>> getInstruments() {
        return ResponseEntity.ok(getInstrumentsUseCase.getInstruments());
    }

    /**
     * Devuelve la lista de precios de mercado actuales.
     */
    @GetMapping("/prices")
    public ResponseEntity<List<MarketPrice>> getPrices() {
        return ResponseEntity.ok(getCurrentPricesUseCase.getCurrentPrices());
    }
}

