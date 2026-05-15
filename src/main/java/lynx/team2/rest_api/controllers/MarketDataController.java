package lynx.team2.rest_api.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lynx.team2.rest_api.internal.Platform;
import lynx.team2.rest_api.models.*;
import lynx.team2.rest_api.services.MarketDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/market")
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final String marketEventsUrl;

    public MarketDataController(
            MarketDataService marketDataService,
            @Value("${market-events.url:http://market-events:8082}") String marketEventsUrl) {
        this.marketDataService = marketDataService;
        this.marketEventsUrl = marketEventsUrl;
    }

    /**
     * GET /market/status <br>
     * TODO: Replace with actual data
     * @return The current state of the market as {@link MarketStatus}
     */
    @GetMapping("/status")
    public MarketStatus getMarketStatus(HttpServletRequest request) {
        Platform platform = (Platform) request.getAttribute("platform");

        System.out.println("Request from: " + platform.getName());
        // TODO: Fetch from live market state service
        return new MarketStatus(true, System.currentTimeMillis() / 1000, System.currentTimeMillis() / 1000, 60, null);
    }

    /**
     * GET /market/stocks <br>
     * @return A list of {@link Stock}, all listed stocks with current prices and simulation metadata
     */
    @GetMapping("/stocks")
    public List<Stock> getStocks() {
        return marketDataService.getAllStocks();
    }

    /**
     * GET /market/stocks:ticker <br>
     * @return The full details for a single stock as {@link Stock}, including OHLC data for the current simulated day.
     */
    @GetMapping("/stocks/{ticker}")
    public Stock getStockByTicker(@PathVariable("ticker") String ticker) {
        Stock stock = marketDataService.getStock(ticker);
        if (stock == null) {
            throw new RuntimeException("Stock not found: " + ticker);
        }
        return stock;
    }

    /**
     * GET /market/stocks:ticker/history <br>
     * TODO: Replace with actual data <br>
     * Query params:<br>
     *     interval - tick | minute | hour (simulated time resolution) <br>
     *     from - ISO 8601 simulated start time <br>
     *     to - ISO 8601 simulated end time
     * @return Historical OHLC price data for charting
     */
    @GetMapping("/stocks/{ticker}/history")
    public List<OHLCPoint> getStockHistoryByTicker(@PathVariable("ticker") String ticker) {
        // TODO: Fetch from historical data service
        return new ArrayList<>();
    }

    /**
     * GET /market/stocks:ticker/orderbook <br>
     * TODO: Replace with actual data
     * @return The current order book depth (top N bid and ask levels) for a stock
     */
    @GetMapping("/stocks/{ticker}/orderbook")
    public OrderBook getStockOrderbookByTicker(@PathVariable("ticker") String ticker) {
        // TODO: Fetch from order book engine
        return new OrderBook(ticker, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * GET /market/options <br>
     * @return All active option contracts with current premiums
     */
    @GetMapping("/options")
    public List<Map<String, Object>> getOptions() {
        return marketDataService.getAllOptions();
    }

    /**
     * GET /market/options/:option_id <br>
     * TODO: Replace with actual data
     * @return Full details for a specific option contract as {@link OptionContract}
     */
    @GetMapping("/options/{option_id}")
    public OptionContract getOptionById(@PathVariable("option_id") String option_id) {
        // TODO: Fetch from options service
        throw new RuntimeException("Option not found: " + option_id);
    }

    /**
     * GET /market/events
     * @return Recent and active market events proxied from the market-events service DB.
     */
    @GetMapping("/events")
    public List<Map<String, Object>> getEvents() {
        try {
            List<Map<String, Object>> result = RestClient.builder().baseUrl(marketEventsUrl).build()
                    .get()
                    .uri("/api/v1/market/events")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            System.out.println("Warning: could not fetch events from market-events: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
