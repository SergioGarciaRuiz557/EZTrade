package com.trading.platform.eztrade.market.adapter.in.web;

import com.trading.platform.eztrade.market.application.ports.in.GetPriceUserCase;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para consultar instrumentos y precios de mercado.
 */
@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    private final GetPriceUserCase getPriceUserCase;

    public MarketController(GetPriceUserCase getPriceUserCase) {
        this.getPriceUserCase = getPriceUserCase;
    }


    @GetMapping("/get-price")
    public ResponseEntity<MarketPrice> getInstruments(@RequestParam Symbol symbol) {
        return ResponseEntity.ok(getPriceUserCase.getPrice(symbol));
    }


}

