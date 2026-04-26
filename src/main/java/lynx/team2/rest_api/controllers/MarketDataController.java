package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.models.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
public class MarketDataController {

    /**
     * GET /market/status <br>
     * TODO: Replace with actual data
     * @return The current state of the market as {@link MarketStatus}
     */
    @GetMapping("/status")
    public MarketStatus getMarketStatus() {
        return MarketStatus.getDummy();
    }

    /**
     * GET /market/stocks <br>
     * TODO: Replace with actual data
     * @return A list of {@link Stock}, all listed stocks with current prices and simulation metadata
     */
    @GetMapping("/stocks")
    public List<Stock> getStocks() {
        List<Stock> stocks = new ArrayList<>();
        stocks.add(Stock.getDummy("ARKA"));
        return stocks;
    }

    /**
     * GET /market/stocks:ticker <br>
     * TODO: Replace with actual data
     * @return The full details for a single stock as {@link Stock}, including OHLC data for the current simulated day.
     */
    @GetMapping("/stocks/{ticker}")
    public Stock getStockByTicker(@PathVariable("ticker") String ticker) {
        return Stock.getDummy(ticker);
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
        List<OHLCPoint> points = new ArrayList<>();
        points.add(OHLCPoint.getDummy(0));
        points.add(OHLCPoint.getDummy(1));
        points.add(OHLCPoint.getDummy(2));
        return points;
    }

    /**
     * GET /market/stocks:ticker/orderbook <br>
     * TODO: Replace with actual data
     * @return The current order book depth (top N bid and ask levels) for a stock
     */
    @GetMapping("/stocks/{ticker}/orderbook")
    public OrderBook getStockOrderbookByTicker(@PathVariable("ticker") String ticker) {
        List<OrderBookPoint> asks = new ArrayList<>();
        asks.add(OrderBookPoint.getDummy(0));
        asks.add(OrderBookPoint.getDummy(1));
        asks.add(OrderBookPoint.getDummy(2));

        List<OrderBookPoint> bids = new ArrayList<>();
        bids.add(OrderBookPoint.getDummy(-1));
        bids.add(OrderBookPoint.getDummy(-2));
        bids.add(OrderBookPoint.getDummy(-3));

        return new OrderBook(ticker, asks, bids);
    }

    /**
     * GET /market/options <br>
     * TODO: Replace with actual data
     * @return All active option contracts with current premiums as a list of {@link OptionContract}
     */
    @GetMapping("/options")
    public List<OptionContract> getOptions() {
        List<OptionContract> options = new ArrayList<>();
        options.add(OptionContract.getDummy("option-abc-123"));
        return options;
    }

    /**
     * GET /market/options/:option_id <br>
     * TODO: Replace with actual data
     * @return Full details for a specific option contract as {@link OptionContract}
     */
    @GetMapping("/options/{option_id}")
    public OptionContract getOptionById(@PathVariable("option_id") String option_id) {
        return OptionContract.getDummy(option_id);
    }

    /**
     * GET /market/events <br>
     * TODO: Replace with actual data
     * @return A list of recent and active market events
     */
    @GetMapping("/events")
    public List<MarketEvent> getEvents() {
        List<MarketEvent> events = new ArrayList<>();
        events.add(MarketEvent.getDummy("event-abc-123"));
        return events;
    }
}
