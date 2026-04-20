package com.trading.platform.eztrade.market.adapter.in;

import com.trading.platform.eztrade.market.application.ports.in.GetOverviewUserCase;
import com.trading.platform.eztrade.market.application.ports.in.GetPriceUserCase;
import com.trading.platform.eztrade.market.application.ports.in.GetDailyCandlesUserCase;
import com.trading.platform.eztrade.market.application.ports.in.SearchInstrumentUserCase;
import com.trading.platform.eztrade.market.domain.Candle;
import com.trading.platform.eztrade.market.domain.Instrument;
import com.trading.platform.eztrade.market.domain.InstrumentOverview;
import com.trading.platform.eztrade.market.domain.MarketPrice;
import com.trading.platform.eztrade.market.domain.Symbol;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para consultar información de mercado.
 * <p>
 * Expone operaciones de la capa de aplicación relacionadas con:
 * <ul>
 *     <li>Obtención del precio actual de un símbolo.</li>
 *     <li>Búsqueda de instrumentos por texto.</li>
 *     <li>Consulta de información fundamental (overview) de un instrumento.</li>
 * </ul>
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

    /**
     * Devuelve el precio de mercado actual para el símbolo indicado.
     * <p>
     * Internamente, delega en el caso de uso {@link GetPriceUserCase}, que a su vez
     * utiliza el proveedor de mercado configurado (por ejemplo, Alpha Vantage) para
     * obtener la cotización en tiempo casi real.
     *
     * @param symbol símbolo bursátil del instrumento (por ejemplo, IBM, AAPL...).
     * @return {@link MarketPrice} con el precio actual y la marca de tiempo.
     */
    @GetMapping("/get-price")
    public ResponseEntity<MarketPrice> getMarketPrice(@RequestParam Symbol symbol) {
        return ResponseEntity.ok(getPriceUserCase.getPrice(symbol));
    }

    /**
     * Busca instrumentos en el mercado cuyo símbolo o nombre coincidan (total o parcialmente)
     * con el texto de entrada.
     * <p>
     * Este endpoint expone el caso de uso {@link SearchInstrumentUserCase} y devuelve
     * una lista de instrumentos básicos que contienen, entre otros datos, el ticker,
     * el nombre, la región de cotización y la moneda.
     *
     * @param input texto introducido por el usuario para filtrar instrumentos (por ejemplo, "IBM", "Apple").
     * @return lista de {@link Instrument} que coinciden con el criterio de búsqueda.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Instrument>> searchInstruments(@RequestParam String input) {
        return ResponseEntity.ok(searchInstrumentUserCase.searchInstruments(input));
    }

    /**
     * Devuelve un overview (información fundamental) del instrumento identificado por el símbolo.
     * <p>
     * Este endpoint expone el caso de uso {@link GetOverviewUserCase} y devuelve datos como
     * sector, industria, capitalización de mercado y PER, obtenidos a través del proveedor
     * de mercado configurado (por ejemplo, Alpha Vantage).
     *
     * @param symbol símbolo bursátil del instrumento (por ejemplo, IBM, AAPL...).
     * @return {@link InstrumentOverview} con los datos fundamentales del instrumento.
     */
    @GetMapping("/get-overview")
    public ResponseEntity<InstrumentOverview> getOverview(@RequestParam Symbol symbol) {
        return ResponseEntity.ok(getOverviewUserCase.getOverview(symbol));
    }

    @GetMapping("/get-daily-candles")
    public ResponseEntity<List<Candle>> getDailyCandles(@RequestParam Symbol symbol) {
        return ResponseEntity.ok(getDailyCandlesUserCase.getDailyCandles(symbol));
    }


}
