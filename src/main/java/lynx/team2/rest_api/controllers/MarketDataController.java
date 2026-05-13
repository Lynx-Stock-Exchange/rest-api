package lynx.team2.rest_api.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lynx.team2.rest_api.exceptions.ErrorCode;
import lynx.team2.rest_api.exceptions.ExchangeException;
import lynx.team2.rest_api.models.*;
import lynx.team2.rest_api.state.StateStore;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/market")
public class MarketDataController {

    private final StateStore stateStore;

    public MarketDataController(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    @GetMapping("/status")
    public MarketStatus getMarketStatus(HttpServletRequest request) {
        return stateStore.getMarketStatus();
    }

    @GetMapping("/stocks")
    public List<Stock> getStocks() {
        return new ArrayList<>(stateStore.getAllStocks());
    }

    @GetMapping("/stocks/{ticker}")
    public Stock getStockByTicker(@PathVariable("ticker") String ticker) {
        return stateStore.getStock(ticker)
                .orElseThrow(() -> new ExchangeException(ErrorCode.INVALID_TICKER, "Ticker not found: " + ticker));
    }

    @GetMapping("/stocks/{ticker}/history")
    public List<OHLCPoint> getStockHistoryByTicker(
            @PathVariable("ticker") String ticker,
            @RequestParam(value = "interval", required = false) String interval,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to) {

        if (stateStore.getStock(ticker).isEmpty()) {
            throw new ExchangeException(ErrorCode.INVALID_TICKER, "Ticker not found: " + ticker);
        }
        return stateStore.getOHLCHistory(ticker);
    }

    @GetMapping("/stocks/{ticker}/orderbook")
    public OrderBook getStockOrderbookByTicker(@PathVariable("ticker") String ticker) {
        if (stateStore.getStock(ticker).isEmpty()) {
            throw new ExchangeException(ErrorCode.INVALID_TICKER, "Ticker not found: " + ticker);
        }

        // Aggregate bids and asks by price level from in-memory orders
        Map<Double, Integer> bidMap = new TreeMap<>(Collections.reverseOrder());
        Map<Double, Integer> askMap = new TreeMap<>();

        for (Order order : stateStore.getAllOrders()) {
            if (!ticker.equals(order.getInstrument_id())) continue;
            if (order.getLimit_price() == null) continue;
            if (!"PENDING".equals(order.getStatus()) && !"PARTIALLY_FILLED".equals(order.getStatus())) continue;

            int remaining = order.getQuantity() - (order.getFilled_quantity() != null ? order.getFilled_quantity() : 0);
            if (remaining <= 0) continue;

            if ("BUY".equals(order.getSide())) {
                bidMap.merge(order.getLimit_price(), remaining, Integer::sum);
            } else if ("SELL".equals(order.getSide())) {
                askMap.merge(order.getLimit_price(), remaining, Integer::sum);
            }
        }

        List<OrderBookPoint> bids = new ArrayList<>();
        bidMap.forEach((price, qty) -> bids.add(new OrderBookPoint(price, qty)));

        List<OrderBookPoint> asks = new ArrayList<>();
        askMap.forEach((price, qty) -> asks.add(new OrderBookPoint(price, qty)));

        return new OrderBook(ticker, bids, asks);
    }

    @GetMapping("/options")
    public List<OptionContract> getOptions() {
        return stateStore.getAllOptions().stream()
                .filter(OptionContract::isIs_active)
                .collect(Collectors.toList());
    }

    @GetMapping("/options/{option_id}")
    public OptionContract getOptionById(@PathVariable("option_id") String optionId) {
        return stateStore.getOption(optionId)
                .orElseThrow(() -> new ExchangeException(ErrorCode.INVALID_TICKER, "Option not found: " + optionId));
    }

    @GetMapping("/events")
    public List<MarketEvent> getEvents() {
        return stateStore.getRecentEvents();
    }
}
