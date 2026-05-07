package lynx.team2.rest_api.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lynx.team2.rest_api.LUtils;
import lynx.team2.rest_api.entities.*;
import lynx.team2.rest_api.exceptions.ErrorCode;
import lynx.team2.rest_api.exceptions.ExchangeException;
import lynx.team2.rest_api.models.*;
import lynx.team2.rest_api.repositories.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
public class MarketDataController {

    private final StockRepository stockRepository;
    private final OHLCPointRepository ohlcRepository;
    private final OptionContractRepository optionRepository;
    private final MarketEventRepository eventRepository;
    private final MarketStateRepository marketStateRepository;
    private final OrderRepository orderRepository;

    public MarketDataController(StockRepository stockRepository,
                                OHLCPointRepository ohlcRepository,
                                OptionContractRepository optionRepository,
                                MarketEventRepository eventRepository,
                                MarketStateRepository marketStateRepository,
                                OrderRepository orderRepository) {
        this.stockRepository = stockRepository;
        this.ohlcRepository = ohlcRepository;
        this.optionRepository = optionRepository;
        this.eventRepository = eventRepository;
        this.marketStateRepository = marketStateRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/status")
    public MarketStatus getMarketStatus(HttpServletRequest request) {
        return marketStateRepository.findById(1)
                .map(s -> new MarketStatus(
                        s.getIsOpen(),
                        s.getMarketTime() != null ? s.getMarketTime() : 0L,
                        s.getRealTime() != null ? s.getRealTime() : 0L,
                        s.getSpeedMultiplier() != null ? s.getSpeedMultiplier() : 1,
                        s.getActiveEventId()
                ))
                .orElse(new MarketStatus(false, 0L, 0L, 1, null));
    }

    @GetMapping("/stocks")
    public List<Stock> getStocks() {
        List<Stock> result = new ArrayList<>();
        for (StockEntity e : stockRepository.findAll()) {
            result.add(toStock(e));
        }
        return result;
    }

    @GetMapping("/stocks/{ticker}")
    public Stock getStockByTicker(@PathVariable("ticker") String ticker) {
        return stockRepository.findById(ticker)
                .map(this::toStock)
                .orElseThrow(() -> new ExchangeException(ErrorCode.INVALID_TICKER, "Ticker not found: " + ticker));
    }

    @GetMapping("/stocks/{ticker}/history")
    public List<OHLCPoint> getStockHistoryByTicker(
            @PathVariable("ticker") String ticker,
            @RequestParam(value = "interval", required = false) String interval,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to) {

        if (stockRepository.findById(ticker).isEmpty()) {
            throw new ExchangeException(ErrorCode.INVALID_TICKER, "Ticker not found: " + ticker);
        }

        Long fromEpoch = from != null ? LUtils.isoToEpochSecond(from) : null;
        Long toEpoch = to != null ? LUtils.isoToEpochSecond(to) : null;

        List<OHLCPoint> result = new ArrayList<>();
        for (OHLCPointEntity e : ohlcRepository.findByTickerAndFilters(ticker, interval, fromEpoch, toEpoch)) {
            result.add(new OHLCPoint(
                    e.getClosePrice() != null ? e.getClosePrice() : 0.0,
                    e.getOpenPrice() != null ? e.getOpenPrice() : 0.0,
                    e.getHighPrice() != null ? e.getHighPrice() : 0.0,
                    e.getLowPrice() != null ? e.getLowPrice() : 0.0,
                    e.getVolume() != null ? e.getVolume() : 0,
                    e.getTimestamp()
            ));
        }
        return result;
    }

    @GetMapping("/stocks/{ticker}/orderbook")
    public OrderBook getStockOrderbookByTicker(@PathVariable("ticker") String ticker) {
        if (stockRepository.findById(ticker).isEmpty()) {
            throw new ExchangeException(ErrorCode.INVALID_TICKER, "Ticker not found: " + ticker);
        }

        List<OrderEntity> activeOrders = orderRepository.findActiveOrdersForOrderBook(ticker);

        // Aggregate bids and asks by price level
        java.util.Map<Double, Integer> bidMap = new java.util.TreeMap<>(java.util.Collections.reverseOrder());
        java.util.Map<Double, Integer> askMap = new java.util.TreeMap<>();

        for (OrderEntity order : activeOrders) {
            if (order.getLimitPrice() == null) continue;
            int remaining = order.getQuantity() - (order.getFilledQuantity() != null ? order.getFilledQuantity() : 0);
            if (remaining <= 0) continue;

            if ("BUY".equals(order.getSide())) {
                bidMap.merge(order.getLimitPrice(), remaining, Integer::sum);
            } else if ("SELL".equals(order.getSide())) {
                askMap.merge(order.getLimitPrice(), remaining, Integer::sum);
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
        List<OptionContract> result = new ArrayList<>();
        for (OptionContractEntity e : optionRepository.findByIsActiveTrue()) {
            result.add(toOptionContract(e));
        }
        return result;
    }

    @GetMapping("/options/{option_id}")
    public OptionContract getOptionById(@PathVariable("option_id") String optionId) {
        return optionRepository.findById(optionId)
                .map(this::toOptionContract)
                .orElseThrow(() -> new ExchangeException(ErrorCode.INVALID_TICKER, "Option not found: " + optionId));
    }

    @GetMapping("/events")
    public List<MarketEvent> getEvents() {
        List<MarketEvent> result = new ArrayList<>();
        for (MarketEventEntity e : eventRepository.findRecentEvents()) {
            result.add(new MarketEvent(
                    e.getEventId(),
                    e.getEventType(),
                    e.getScope(),
                    e.getTarget(),
                    e.getMagnitude() != null ? e.getMagnitude() : 0.0,
                    e.getDurationTicks() != null ? e.getDurationTicks() : 0,
                    e.getHeadline(),
                    e.getTriggeredAt() != null ? e.getTriggeredAt() : 0L,
                    e.getTriggeredBy()
            ));
        }
        return result;
    }

    private Stock toStock(StockEntity e) {
        return new Stock(
                e.getTicker(),
                e.getName(),
                e.getSector(),
                e.getCurrentPrice() != null ? e.getCurrentPrice() : 0.0,
                e.getOpenPrice() != null ? e.getOpenPrice() : 0.0,
                e.getHighPrice() != null ? e.getHighPrice() : 0.0,
                e.getLowPrice() != null ? e.getLowPrice() : 0.0,
                e.getVolume() != null ? e.getVolume() : 0,
                e.getVolatility() != null ? e.getVolatility() : 0.0,
                e.getTrendBias() != null ? e.getTrendBias() : 0.0,
                e.getEventWeight() != null ? e.getEventWeight() : 0.0,
                e.getMomentum() != null ? e.getMomentum() : 0.0,
                e.getListedAt() != null ? e.getListedAt() : 0L
        );
    }

    private OptionContract toOptionContract(OptionContractEntity e) {
        return new OptionContract(
                e.getOptionId(),
                e.getUnderlyingTicker(),
                e.getOptionType(),
                e.getStrikePrice() != null ? e.getStrikePrice() : 0.0,
                e.getExpiryTime() != null ? e.getExpiryTime() : 0L,
                e.getPremium() != null ? e.getPremium() : 0.0,
                e.getIsActive() != null && e.getIsActive(),
                e.getAutoExercise() != null && e.getAutoExercise()
        );
    }
}
