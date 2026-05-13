package lynx.team2.rest_api.state;

import lynx.team2.rest_api.models.*;
import lynx.team2.rest_api.internal.Platform;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

@Service
public class StateStore {
    private final Map<String, Stock> stocks = new ConcurrentHashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Map<String, OptionContract> options = new ConcurrentHashMap<>();
    private final Map<String, MarketEvent> marketEvents = new ConcurrentHashMap<>();
    private final Map<String, Platform> platforms = new ConcurrentHashMap<>();
    private final Map<String, Trade> trades = new ConcurrentHashMap<>();
    private volatile MarketStatus marketStatus = new MarketStatus(false, 0L, 0L, 1, null);
    private volatile double feeRate = 0.001;

    // Local platform credentials (for platforms created via REST API admin endpoint)
    static class LocalPlatformCred {
        final String id;
        final String apiSecret;
        final String name;
        LocalPlatformCred(String id, String apiSecret, String name) {
            this.id = id;
            this.apiSecret = apiSecret;
            this.name = name;
        }
    }
    private final Map<String, LocalPlatformCred> localPlatformsByApiKey = new ConcurrentHashMap<>();
    private final Map<String, String> platformIdToApiKey = new ConcurrentHashMap<>();

    // OHLC history per ticker (capped at MAX_OHLC_HISTORY entries)
    private final Map<String, LinkedList<OHLCPoint>> ohlcHistory = new ConcurrentHashMap<>();
    private static final int MAX_OHLC_HISTORY = 1440;

    // Revenue tracking
    private final DoubleAdder totalRevenue = new DoubleAdder();

    // Stocks
    public void updateStock(Stock stock) { stocks.put(stock.getTicker(), stock); }
    public Collection<Stock> getAllStocks() { return List.copyOf(stocks.values()); }
    public Optional<Stock> getStock(String ticker) { return Optional.ofNullable(stocks.get(ticker)); }
    public void removeStock(String ticker) { stocks.remove(ticker); }

    // Orders
    public void updateOrder(Order order) { orders.put(order.getOrder_id(), order); }
    public Optional<Order> getOrder(String orderId) { return Optional.ofNullable(orders.get(orderId)); }
    public List<Order> getAllOrders() { return List.copyOf(orders.values()); }
    public List<Order> getOrdersByPlatform(String platformId) {
        return orders.values().stream()
                .filter(o -> platformId.equals(o.getPlatform_id()))
                .collect(Collectors.toList());
    }

    // Options
    public void updateOption(OptionContract option) { options.put(option.getOption_id(), option); }
    public Collection<OptionContract> getAllOptions() { return List.copyOf(options.values()); }
    public Optional<OptionContract> getOption(String optionId) { return Optional.ofNullable(options.get(optionId)); }
    public void removeOption(String optionId) { options.remove(optionId); }

    // Market Events
    public void addMarketEvent(MarketEvent event) { marketEvents.put(event.getEvent_id(), event); }
    public List<MarketEvent> getRecentEvents() {
        return marketEvents.values().stream()
                .sorted((e1, e2) -> Long.compare(e2.getTriggered_at_long(), e1.getTriggered_at_long()))
                .limit(50)
                .collect(Collectors.toList());
    }

    // Market Status
    public MarketStatus getMarketStatus() { return marketStatus; }
    public synchronized void setMarketStatus(MarketStatus status) { this.marketStatus = status; }
    public synchronized void updateMarketOpen(boolean open) {
        MarketStatus c = this.marketStatus;
        this.marketStatus = new MarketStatus(open, c.getMarket_time_long(), c.getReal_time_long(), c.getSpeed_multiplier(), c.getActive_event_id());
    }
    public synchronized void updateMarketSpeed(int speed) {
        MarketStatus c = this.marketStatus;
        this.marketStatus = new MarketStatus(c.isIs_open(), c.getMarket_time_long(), c.getReal_time_long(), speed, c.getActive_event_id());
    }
    public synchronized void updateActiveEvent(String eventId) {
        MarketStatus c = this.marketStatus;
        this.marketStatus = new MarketStatus(c.isIs_open(), c.getMarket_time_long(), c.getReal_time_long(), c.getSpeed_multiplier(), eventId);
    }
    public synchronized void updateMarketTime(long marketTime) {
        MarketStatus c = this.marketStatus;
        this.marketStatus = new MarketStatus(c.isIs_open(), marketTime, c.getReal_time_long(), c.getSpeed_multiplier(), c.getActive_event_id());
    }
    public synchronized void updateRealTime(long realTime) {
        MarketStatus c = this.marketStatus;
        this.marketStatus = new MarketStatus(c.isIs_open(), c.getMarket_time_long(), realTime, c.getSpeed_multiplier(), c.getActive_event_id());
    }

    // Fees
    public double getFeeRate() { return feeRate; }
    public void setFeeRate(double feeRate) { this.feeRate = feeRate; }

    // Platforms (Kafka-synced set, used for InternalController.getActivePlatforms)
    public void updatePlatform(Platform platform) { platforms.put(platform.getId(), platform); }
    public void removePlatform(String platformId) { platforms.remove(platformId); }
    public Collection<Platform> getAllPlatforms() { return List.copyOf(platforms.values()); }

    // Local platform credentials (created via REST API admin endpoint, verified without admin panel)
    public void registerPlatformCredentials(String apiKey, String apiSecret, String id, String name) {
        localPlatformsByApiKey.put(apiKey, new LocalPlatformCred(id, apiSecret, name));
        platformIdToApiKey.put(id, apiKey);
    }

    public void removeLocalPlatformById(String id) {
        String apiKey = platformIdToApiKey.remove(id);
        if (apiKey != null) localPlatformsByApiKey.remove(apiKey);
    }

    // Returns a Platform if the api_key+api_secret match a locally-registered platform, null otherwise
    public Platform verifyPlatformCredentials(String apiKey, String apiSecret) {
        LocalPlatformCred cred = localPlatformsByApiKey.get(apiKey);
        if (cred != null && cred.apiSecret.equals(apiSecret)) {
            return new Platform(cred.id, cred.name);
        }
        return null;
    }

    // Trades
    public void addTrade(Trade trade) { trades.put(trade.getTrade_id(), trade); }
    public List<Trade> getTradesByPlatformAndUser(String platformId, String platformUserId) {
        return trades.values().stream()
                .filter(t -> platformId.equals(t.getPlatform_id()) && platformUserId.equals(t.getPlatform_user_id()))
                .collect(Collectors.toList());
    }

    // Revenue
    public void addRevenue(double amount) { totalRevenue.add(amount); }
    public double getTotalRevenue() { return totalRevenue.sum(); }

    // OHLC history
    public void addOHLCPoint(String ticker, OHLCPoint point) {
        LinkedList<OHLCPoint> history = ohlcHistory.computeIfAbsent(ticker, k -> new LinkedList<>());
        synchronized (history) {
            history.addLast(point);
            while (history.size() > MAX_OHLC_HISTORY) {
                history.removeFirst();
            }
        }
    }

    public List<OHLCPoint> getOHLCHistory(String ticker) {
        LinkedList<OHLCPoint> history = ohlcHistory.get(ticker);
        if (history == null) return Collections.emptyList();
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }
}