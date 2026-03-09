package com.trading.platform.eztrade.market.adapter.out.external;

import com.trading.platform.eztrade.market.application.ports.in.GetDailyCandlesUserCase;
import com.trading.platform.eztrade.market.application.ports.out.GetOverviewProviderPort;
import com.trading.platform.eztrade.market.application.ports.out.GetPriceMarketProviderPort;
import com.trading.platform.eztrade.market.application.ports.out.SearchInstrumentProviderPort;
import com.trading.platform.eztrade.market.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador que integra la aplicación con la API pública de Alpha Vantage.
 * <p>
 * Esta clase es un componente de infraestructura que se encarga de:
 * <ul>
 *     <li>Obtener el precio actual de un instrumento financiero (acción, ETF, etc.).</li>
 *     <li>Buscar instrumentos por texto (por ejemplo, "IBM", "Apple").</li>
 *     <li>Obtener velas diarias (serie histórica OHLCV) de un símbolo.</li>
 * </ul>
 * <p>
 * Implementa los puertos de la capa de aplicación/dominio y traduce esas operaciones
 * en llamadas HTTP contra la API de Alpha Vantage.
 */
@Component
public class AlphaVantageAPI implements GetPriceMarketProviderPort, SearchInstrumentProviderPort, GetDailyCandlesUserCase, GetOverviewProviderPort {

    /**
     * Clave de la API de Alpha Vantage. Debe configurarse en {@code application.properties}
     * con la propiedad {@code alphaVantage.api.key}.
     */
    @Value("${alphaVantage.api.key}")
    private String apiKey;

    /**
     * URL base de la API de Alpha Vantage, normalmente {@code https://www.alphavantage.co/query}.
     * Se configura mediante la propiedad {@code alphaVantage.api.base-url}.
     */
    @Value("${alphaVantage.api.base-url}")
    private String baseUrl;

    /**
     * Tiempo máximo (en milisegundos) que se esperará para conectar y leer respuestas HTTP.
     * Se configura con {@code alphaVantage.api.timeout}. Si no se define, se usan 5000 ms por defecto.
     */
    @Value("${alphaVantage.api.timeout:5000}")
    private int timeout;

    /**
     * Tiempo de espera mínimo entre peticiones a la API (en milisegundos).
     * Esto ayuda a respetar los límites de uso (rate limits) de la versión gratuita de Alpha Vantage.
     */
    private static final long THROTTLE_MS = 500L;

    /**
     * Realiza una pequeña pausa en el hilo actual para espaciar las peticiones HTTP.
     * <p>
     * Si el hilo es interrumpido mientras duerme, se restaura el estado de interrupción.
     */
    private void throttle() {
        try {
            Thread.sleep(THROTTLE_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Nombre del campo raíz que contiene la cotización en la respuesta del endpoint GLOBAL_QUOTE.
     */
    private static final String GLOBAL_QUOTE_FIELD = "Global Quote";




    /**
     * Obtiene el precio de mercado actual de un símbolo bursátil utilizando el endpoint
     * {@code GLOBAL_QUOTE} de Alpha Vantage.
     *
     * @param symbol símbolo del instrumento (por ejemplo, IBM, AAPL...).
     * @return una instancia de {@link MarketPrice} con el precio actual y la marca de tiempo.
     * @throws ExternalApiException si se produce algún error de comunicación con la API
     *                              o si la respuesta no contiene los datos esperados.
     */
    @Override
    public MarketPrice getMarketPrice(Symbol symbol) {
        // Limitamos la frecuencia de peticiones para no sobrepasar el rate limit.
        throttle();

        // Construimos la URL para el endpoint GLOBAL_QUOTE con el símbolo y la API key.
        System.out.println("Getting market price for " + symbol);
        String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s", baseUrl, symbol.value(), apiKey);

        // Configuramos la factoría de peticiones con los timeouts deseados.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        // Creamos el cliente HTTP de Spring usando la URL base y la factoría anterior.
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();

        JsonNode responseJson;
        try {
            // Realizamos la petición GET y deserializamos la respuesta a un árbol JSON.
            responseJson = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);

            // Comprobamos que el JSON tiene la estructura mínima necesaria.
            if (responseJson == null
                    || responseJson.get(GLOBAL_QUOTE_FIELD) == null
                    || responseJson.get(GLOBAL_QUOTE_FIELD).get("05. price") == null) {
                throw new ExternalApiException("Invalid response from Alpha Vantage API: missing or malformed price data");
            }
        } catch (Exception e) {
            // Cualquier error (timeout, red, parseo...) se envuelve en nuestra excepción de dominio.
            throw new ExternalApiException("Error communicating with Alpha Vantage API. Please check the value for alphaVantage.api.key in application.properties", e);
        }

        double currentPrice;
        try {
            // Extraemos el precio como texto y lo convertimos a double.
            currentPrice = Double.parseDouble(responseJson.get(GLOBAL_QUOTE_FIELD).get("05. price").asString());
        } catch (Exception e) {
            throw new ExternalApiException("Could not parse price from Alpha Vantage response", e);
        }

        // Devolvemos el objeto de dominio con el instante actual (zona Europe/Madrid).
        return new MarketPrice(
                symbol,
                currentPrice,
                LocalDateTime.now().atZone(ZoneId.of("Europe/Madrid")).toInstant()
        );
    }

    /**
     * Busca instrumentos financieros en Alpha Vantage a partir de un texto de entrada
     * usando el endpoint {@code SYMBOL_SEARCH}.
     *
     * @param input texto introducido por el usuario (por ejemplo, "IBM", "Apple", "Micro").
     * @return una lista de {@link Instrument} que coinciden con el criterio. Puede ser vacía
     * si no se obtienen resultados o si la respuesta de la API no contiene el array esperado.
     * @throws ExternalApiException si se produce un error de comunicación con la API externa.
     */
    @Override
    public List<Instrument> searchInstruments(String input) {
        // De nuevo, aplicamos el throttle para respetar los límites de la API.
        throttle();

        // Endpoint SYMBOL_SEARCH con la palabra clave proporcionada por el usuario.
        String url = String.format(
                "%s?function=SYMBOL_SEARCH&keywords=%s&apikey=%s",
                baseUrl, input, apiKey
        );

        // Cliente HTTP con los mismos timeouts que en el resto de métodos.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();

        JsonNode root;
        try {
            root = restClient.get().uri(url).retrieve().body(JsonNode.class);
        } catch (Exception e) {
            throw new ExternalApiException("Error communicating with Alpha Vantage API for instrument search", e);
        }

        // Si la respuesta no tiene el array "bestMatches", no hay datos útiles que devolver.
        if (root == null || root.get("bestMatches") == null || !root.get("bestMatches").isArray()) {
            return List.of();
        }

        List<Instrument> result = new ArrayList<>();

        // Recorremos cada coincidencia y la mapeamos a nuestro objeto de dominio Instrument.
        for (JsonNode node : root.get("bestMatches")) {
            result.add(new Instrument(
                    node.path("1. symbol").asString(),   // Símbolo (ticker)
                    node.path("2. name").asString(),     // Nombre descriptivo
                    node.path("4. region").asString(),   // Región/mercado
                    node.path("8. currency").asString()  // Moneda en la que cotiza
            ));
        }

        return result;
    }


    /**
     * Obtiene velas diarias (OHLCV) para un símbolo bursátil usando el endpoint
     * {@code TIME_SERIES_DAILY} de Alpha Vantage.
     * <p>
     * La API devuelve un objeto JSON donde las claves del nodo {@code "Time Series (Daily)"}
     * son fechas en formato ISO ({@code yyyy-MM-dd}), y cada valor es un objeto con:
     * <ul>
     *     <li>{@code 1. open}: precio de apertura</li>
     *     <li>{@code 2. high}: máximo diario</li>
     *     <li>{@code 3. low}: mínimo diario</li>
     *     <li>{@code 4. close}: precio de cierre</li>
     *     <li>{@code 5. volume}: volumen negociado</li>
     * </ul>
     * Este método recorre, desde la fecha actual hacia atrás, un máximo de 60 días y,
     * para cada fecha que tenga datos, construye una {@link Candle} con esa información.
     *
     * @param symbol símbolo para el que se quiere obtener la serie histórica diaria.
     * @return lista de velas diarias. Si la API no devuelve datos para el símbolo, la lista
     * será vacía.
     * @throws ExternalApiException si ocurre un error al comunicarse con Alpha Vantage.
     */
    @Override
    public List<Candle> getDailyCandles(Symbol symbol) {
        // Aplicamos el throttle antes de consultar la serie histórica.
        throttle();

        // Construimos la URL para el endpoint TIME_SERIES_DAILY.
        String url = String.format(
                "%s?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s",
                baseUrl, symbol.value(), apiKey
        );

        // Cliente HTTP configurado con los timeouts de la aplicación.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();

        JsonNode root;
        try {
            root = restClient.get().uri(url).retrieve().body(JsonNode.class);
        } catch (Exception e) {
            throw new ExternalApiException("Error communicating with Alpha Vantage API for daily candles", e);
        }

        // Si no hay nodo "Time Series (Daily)", no hay datos históricos disponibles.
        if (root == null || root.get("Time Series (Daily)") == null) {
            return List.of();
        }

        JsonNode series = root.get("Time Series (Daily)");

        List<Candle> candles = new ArrayList<>();

        // Tomamos como referencia la fecha actual y miramos hacia atrás un máximo de 60 días.
        LocalDate today = LocalDate.now();
        int maxDays = 60;

        for (int i = 0; i < maxDays; i++) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString(); // Formato ISO: "yyyy-MM-dd"

            // En el JSON de Alpha Vantage, cada fecha es una clave dentro de "Time Series (Daily)".
            JsonNode c = series.get(dateStr);
            if (c == null) {
                // Si para esa fecha no hay datos (por ejemplo, fin de semana o límite de histórico), se salta.
                continue;
            }

            // Construimos la vela diaria a partir de los campos de la API.
            candles.add(new Candle(
                    date.atStartOfDay(),                 // Momento temporal: inicio del día
                    c.path("1. open").asDouble(),       // Precio de apertura
                    c.path("2. high").asDouble(),       // Máximo
                    c.path("3. low").asDouble(),        // Mínimo
                    c.path("4. close").asDouble(),      // Cierre
                    c.path("5. volume").asLong()        // Volumen
            ));
        }

        return candles;
    }

    /**
     * Obtiene información fundamental (overview) de un instrumento usando el endpoint
     * {@code OVERVIEW} de Alpha Vantage.
     * <p>
     * Este endpoint devuelve datos fundamentales como sector, industria,
     * capitalización de mercado, PER, etc. A partir de esa respuesta se construye
     * un {@link InstrumentOverview} de dominio.
     *
     * @param symbol símbolo para el que se quiere obtener el overview (por ejemplo, IBM, AAPL...).
     * @return un {@link InstrumentOverview} con los datos fundamentales básicos del símbolo.
     * @throws ExternalApiException si se produce un error de comunicación con la API externa
     *                              o si la respuesta no contiene los campos esperados.
     */
    @Override
    public InstrumentOverview getOverview(Symbol symbol) {
        // Respetamos el rate limit de Alpha Vantage
        throttle();

        // Endpoint OVERVIEW para obtener datos fundamentales del símbolo
        String url = String.format(
                "%s?function=OVERVIEW&symbol=%s&apikey=%s",
                baseUrl, symbol.value(), apiKey
        );

        // Cliente HTTP con los mismos timeouts que en el resto de métodos.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();

        JsonNode node;
        try {
            // Realizamos la petición y obtenemos el JSON de respuesta
            node = restClient.get().uri(url).retrieve().body(JsonNode.class);
        } catch (Exception e) {
            throw new ExternalApiException("Error communicating with Alpha Vantage API for instrument overview", e);
        }

        // Validación mínima de la respuesta: si es nula o no tiene el campo Symbol,
        // consideramos que la respuesta no es válida.
        if (node == null || node.get("Symbol") == null) {
            throw new ExternalApiException("Invalid response from Alpha Vantage API: missing overview data");
        }

        // Mapeamos los campos relevantes de la respuesta JSON a nuestro objeto de dominio.
        return new InstrumentOverview(
                node.get("Symbol").asString(),
                node.get("Name").asString(),
                node.get("Sector").asString(),
                node.get("Industry").asString(),
                node.get("MarketCapitalization").asLong(),
                node.get("PERatio").asDouble()
        );
    }
}
