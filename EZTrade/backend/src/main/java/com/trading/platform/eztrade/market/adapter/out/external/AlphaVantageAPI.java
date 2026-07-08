package com.trading.platform.eztrade.market.adapter.out.external;

import com.trading.platform.eztrade.market.application.ports.out.GetDailyCandlesProviderPort;
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
 * Adapter that integrates the application with the public Alpha Vantage API.
 * <p>
 * This infrastructure component is responsible for:
 * <ul>
 *     <li>Obtaining the current price of a financial instrument (stock, ETF, etc.).</li>
 *     <li>Searching instruments by text (for example, "IBM", "Apple").</li>
 *     <li>Obtaining daily candles (historical OHLCV series) for a symbol.</li>
 * </ul>
 * <p>
 * It implements the application/domain layer ports and translates those
 * operations into HTTP calls against the Alpha Vantage API.
 */
@Component("marketDataProvider")
public class AlphaVantageAPI implements GetPriceMarketProviderPort, SearchInstrumentProviderPort, GetDailyCandlesProviderPort, GetOverviewProviderPort {

    /**
     * Alpha Vantage API key. Must be configured in {@code application.properties}
     * with the {@code alphaVantage.api.key} property.
     */
    @Value("${alphaVantage.api.key}")
    private String apiKey;

    /**
     * Base URL of the Alpha Vantage API, usually {@code https://www.alphavantage.co/query}.
     * Configured through the {@code alphaVantage.api.base-url} property.
     */
    @Value("${alphaVantage.api.base-url}")
    private String baseUrl;

    /**
     * Maximum time (in milliseconds) to wait for HTTP connection and read operations.
     * Configured with {@code alphaVantage.api.timeout}. Defaults to 5000 ms when not defined.
     */
    @Value("${alphaVantage.api.timeout:5000}")
    private int timeout;

    /**
     * Minimum waiting time between API requests (in milliseconds).
     * This helps respect the usage limits (rate limits) of the free Alpha Vantage tier.
     */
    @Value("${alphaVantage.api.min-interval-ms:1100}")
    private long minIntervalMs;

    /**
     * Performs a short pause in the current thread to space out HTTP requests.
     * <p>
     * If the thread is interrupted while sleeping, the interrupted status is restored.
     */
    private void throttle() {
        try {
            Thread.sleep(minIntervalMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void ensureApiResponseIsUsable(JsonNode node, String function) {
        if (node == null) {
            throw new ExternalApiException("Empty response from Alpha Vantage for function " + function);
        }

        if (node.path("Information").isTextual()) {
            throw new ExternalApiException("Alpha Vantage rate limit reached: " + node.path("Information").asText());
        }

        if (node.path("Note").isTextual()) {
            throw new ExternalApiException("Alpha Vantage note for " + function + ": " + node.path("Note").asText());
        }

        if (node.path("Error Message").isTextual()) {
            throw new ExternalApiException("Alpha Vantage error for " + function + ": " + node.path("Error Message").asText());
        }
    }

    /**
     * Root field name that contains the quote in the GLOBAL_QUOTE endpoint response.
     */
    private static final String GLOBAL_QUOTE_FIELD = "Global Quote";




    /**
     * Obtains the current market price of a stock symbol using Alpha Vantage's
     * {@code GLOBAL_QUOTE} endpoint.
     *
     * @param symbol instrument symbol (for example, IBM, AAPL...)
     * @return {@link MarketPrice} instance with the current price and timestamp
     * @throws ExternalApiException if an API communication error occurs or if
     *                              the response does not contain the expected data
     */
    @Override
    public MarketPrice getMarketPrice(Symbol symbol) {
        // Limit request frequency to avoid exceeding the rate limit.
        throttle();

        // Build the URL for the GLOBAL_QUOTE endpoint with the symbol and API key.
        System.out.println("Getting market price for " + symbol);
        String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s", baseUrl, symbol.value(), apiKey);

        // Configure the request factory with the desired timeouts.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        // Create the Spring HTTP client using the base URL and the factory above.
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();

        JsonNode responseJson;
        try {
            // Perform the GET request and deserialize the response into a JSON tree.
            responseJson = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);

            ensureApiResponseIsUsable(responseJson, "GLOBAL_QUOTE");

            // Check that the JSON has the minimum required structure.
            if (responseJson.get(GLOBAL_QUOTE_FIELD) == null
                    || responseJson.get(GLOBAL_QUOTE_FIELD).get("05. price") == null) {
                throw new ExternalApiException("Invalid response from Alpha Vantage API: missing or malformed price data");
            }
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            // Wrap any error (timeout, network, parsing...) in our domain exception.
            throw new ExternalApiException("Error communicating with Alpha Vantage API. Please check the value for alphaVantage.api.key in application.properties", e);
        }

        double currentPrice;
        try {
            // Extract the price as text and convert it to double.
            currentPrice = Double.parseDouble(responseJson.get(GLOBAL_QUOTE_FIELD).get("05. price").asString());
        } catch (Exception e) {
            throw new ExternalApiException("Could not parse price from Alpha Vantage response", e);
        }

        // Return the domain object with the current instant (Europe/Madrid zone).
        return new MarketPrice(
                symbol,
                currentPrice,
                LocalDateTime.now().atZone(ZoneId.of("Europe/Madrid")).toInstant()
        );
    }

    /**
     * Searches financial instruments in Alpha Vantage from input text using the
     * {@code SYMBOL_SEARCH} endpoint.
     *
     * @param input text entered by the user (for example, "IBM", "Apple", "Micro")
     * @return list of {@link Instrument} items matching the criterion. It may be
     * empty if no results are obtained or if the API response does not contain the expected array
     * @throws ExternalApiException if a communication error occurs with the external API
     */
    @Override
    public List<Instrument> searchInstruments(String input) {
        // Apply throttling again to respect the API limits.
        throttle();

        // SYMBOL_SEARCH endpoint with the keyword provided by the user.
        String url = String.format(
                "%s?function=SYMBOL_SEARCH&keywords=%s&apikey=%s",
                baseUrl, input, apiKey
        );

        // HTTP client with the same timeouts used by the other methods.
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
            ensureApiResponseIsUsable(root, "SYMBOL_SEARCH");
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalApiException("Error communicating with Alpha Vantage API for instrument search", e);
        }

        // If the response does not have the "bestMatches" array, there is no useful data to return.
        if (root == null || root.get("bestMatches") == null || !root.get("bestMatches").isArray()) {
            return List.of();
        }

        List<Instrument> result = new ArrayList<>();

        // Iterate over each match and map it to our Instrument domain object.
        for (JsonNode node : root.get("bestMatches")) {
            result.add(new Instrument(
                    node.path("1. symbol").asString(),   // Symbol (ticker)
                    node.path("2. name").asString(),     // Descriptive name
                    node.path("4. region").asString(),   // Region/market
                    node.path("8. currency").asString()  // Trading currency
            ));
        }

        return result;
    }


    /**
     * Obtains daily candles (OHLCV) for a stock symbol using Alpha Vantage's
     * {@code TIME_SERIES_DAILY} endpoint.
     * <p>
     * The API returns a JSON object where the keys of the {@code "Time Series (Daily)"}
     * node are ISO dates ({@code yyyy-MM-dd}), and each value is an object with:
     * <ul>
     *     <li>{@code 1. open}: opening price</li>
     *     <li>{@code 2. high}: daily high</li>
     *     <li>{@code 3. low}: daily low</li>
     *     <li>{@code 4. close}: closing price</li>
     *     <li>{@code 5. volume}: traded volume</li>
     * </ul>
     * This method walks backward from the current date for up to 60 days and,
     * for each date with data, builds a {@link Candle} with that information.
     *
     * @param symbol symbol for which the daily historical series should be obtained
     * @return list of daily candles. If the API returns no data for the symbol, the list is empty
     * @throws ExternalApiException if an error occurs while communicating with Alpha Vantage
     */
    @Override
    public List<Candle> getDailyCandles(Symbol symbol) {
        // Apply throttling before querying the historical series.
        throttle();

        // Build the URL for the TIME_SERIES_DAILY endpoint.
        String url = String.format(
                "%s?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s",
                baseUrl, symbol.value(), apiKey
        );

        // HTTP client configured with the application timeouts.
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
            ensureApiResponseIsUsable(root, "TIME_SERIES_DAILY");
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalApiException("Error communicating with Alpha Vantage API for daily candles", e);
        }

        // If there is no "Time Series (Daily)" node, there is no historical data available.
        if (root == null || root.get("Time Series (Daily)") == null) {
            return List.of();
        }

        JsonNode series = root.get("Time Series (Daily)");

        List<Candle> candles = new ArrayList<>();

        // Use the current date as reference and look backward for up to 60 days.
        LocalDate today = LocalDate.now();
        int maxDays = 60;

        for (int i = 0; i < maxDays; i++) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString(); // ISO format: "yyyy-MM-dd"

            // In Alpha Vantage JSON, each date is a key inside "Time Series (Daily)".
            JsonNode c = series.get(dateStr);
            if (c == null) {
                // If there is no data for that date (for example, weekend or history limit), skip it.
                continue;
            }

            // Build the daily candle from the API fields.
            candles.add(new Candle(
                    date.atStartOfDay(),                 // Time: start of day
                    c.path("1. open").asDouble(),       // Opening price
                    c.path("2. high").asDouble(),       // High
                    c.path("3. low").asDouble(),        // Low
                    c.path("4. close").asDouble(),      // Close
                    c.path("5. volume").asLong()        // Volume
            ));
        }

        return candles;
    }

    /**
     * Obtains fundamental information (overview) for an instrument using Alpha
     * Vantage's {@code OVERVIEW} endpoint.
     * <p>
     * This endpoint returns fundamentals such as sector, industry, market
     * capitalization, P/E ratio, etc. An {@link InstrumentOverview} domain object
     * is built from that response.
     *
     * @param symbol symbol whose overview should be obtained (for example, IBM, AAPL...)
     * @return {@link InstrumentOverview} with the symbol's basic fundamentals
     * @throws ExternalApiException if a communication error occurs with the external API
     *                              or if the response does not contain the expected fields
     */
    @Override
    public InstrumentOverview getOverview(Symbol symbol) {
        // Respect the Alpha Vantage rate limit.
        throttle();

        // OVERVIEW endpoint for obtaining the symbol's fundamentals.
        String url = String.format(
                "%s?function=OVERVIEW&symbol=%s&apikey=%s",
                baseUrl, symbol.value(), apiKey
        );

        // HTTP client with the same timeouts used by the other methods.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();

        JsonNode node;
        try {
            // Perform the request and obtain the JSON response.
            node = restClient.get().uri(url).retrieve().body(JsonNode.class);
            ensureApiResponseIsUsable(node, "OVERVIEW");
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalApiException("Error communicating with Alpha Vantage API for instrument overview", e);
        }

        // Minimum response validation: if it is null or has no Symbol field,
        // consider the response invalid.
        if (node.get("Symbol") == null) {
            throw new ExternalApiException("Invalid response from Alpha Vantage API: missing overview data");
        }

        // Map the relevant JSON response fields to our domain object.
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
