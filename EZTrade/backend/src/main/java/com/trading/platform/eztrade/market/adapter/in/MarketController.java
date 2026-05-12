package com.trading.platform.eztrade.market.adapter.in;

import com.trading.platform.eztrade.market.adapter.in.dto.CandleResponse;
import com.trading.platform.eztrade.market.adapter.in.dto.InstrumentOverviewResponse;
import com.trading.platform.eztrade.market.adapter.in.dto.InstrumentResponse;
import com.trading.platform.eztrade.market.adapter.in.dto.MarketPriceResponse;
import com.trading.platform.eztrade.market.application.ports.in.GetDailyCandlesUserCase;
import com.trading.platform.eztrade.market.application.ports.in.GetOverviewUserCase;
import com.trading.platform.eztrade.market.application.ports.in.GetPriceUserCase;
import com.trading.platform.eztrade.market.application.ports.in.SearchInstrumentUserCase;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para consultar informacion de mercado.
 */
@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    private final GetPriceUserCase getPriceUserCase;
    private final SearchInstrumentUserCase searchInstrumentUserCase;
    private final GetOverviewUserCase getOverviewUserCase;
    private final GetDailyCandlesUserCase getDailyCandlesUserCase;

    public MarketController(GetPriceUserCase getPriceUserCase,
                            SearchInstrumentUserCase searchInstrumentUserCase,
                            GetOverviewUserCase getOverviewUserCase,
                            GetDailyCandlesUserCase getDailyCandlesUserCase) {
        this.getPriceUserCase = getPriceUserCase;
        this.searchInstrumentUserCase = searchInstrumentUserCase;
        this.getOverviewUserCase = getOverviewUserCase;
        this.getDailyCandlesUserCase = getDailyCandlesUserCase;
    }

    @GetMapping("/get-price")
    public ResponseEntity<MarketPriceResponse> getMarketPrice(@RequestParam Symbol symbol) {
        return ResponseEntity.ok(MarketPriceResponse.from(getPriceUserCase.getPrice(symbol)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<InstrumentResponse>> searchInstruments(@RequestParam String input) {
        return ResponseEntity.ok(searchInstrumentUserCase.searchInstruments(input)
                .stream()
                .map(InstrumentResponse::from)
                .toList());
    }

    @GetMapping("/get-overview")
    public ResponseEntity<InstrumentOverviewResponse> getOverview(@RequestParam Symbol symbol) {
        return ResponseEntity.ok(InstrumentOverviewResponse.from(getOverviewUserCase.getOverview(symbol)));
    }

    @GetMapping("/get-daily-candles")
    public ResponseEntity<List<CandleResponse>> getDailyCandles(@RequestParam Symbol symbol) {
        return ResponseEntity.ok(getDailyCandlesUserCase.getDailyCandles(symbol)
                .stream()
                .map(CandleResponse::from)
                .toList());
    }
}
