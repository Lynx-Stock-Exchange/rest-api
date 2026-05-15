package lynx.team2.rest_api.services;

import lynx.team2.rest_api.models.Stock;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Service
public class MarketDataService {
    private static final Logger log = Logger.getLogger(MarketDataService.class.getName());
    private final Map<String, Stock> stocks = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> options = new ConcurrentHashMap<>();

    public void saveStock(Stock stock) {
        stocks.put(stock.getTicker(), stock);
    }

    public List<Stock> getAllStocks() {
        return new ArrayList<>(stocks.values());
    }

    public Stock getStock(String ticker) {
        return stocks.get(ticker.toUpperCase());
    }

    public void saveOption(Map<String, Object> option) {
        String id = (String) option.get("option_id");
        if (id != null) {
            options.put(id, option);
        }
    }

    public List<Map<String, Object>> getAllOptions() {
        return new ArrayList<>(options.values());
    }

    @KafkaListener(topics = "market.options", groupId = "rest-api-group")
    public void handleOptionUpdate(Map<String, Object> option) {
        String id = (String) option.get("option_id");
        if (id != null) {
            options.put(id, option);
        }
    }

    @KafkaListener(topics = "stock.prices", groupId = "rest-api-group")
    public void handlePriceUpdate(Map<String, Object> update) {
        String ticker = (String) update.get("ticker");
        if (ticker == null) return;

        stocks.compute(ticker, (t, existing) -> {
            Stock stock = existing;
            if (stock == null) {
                stock = new Stock();
                stock.setTicker(ticker);
                stock.setName(stringVal(update, "name", ticker));
                stock.setSector(stringVal(update, "sector", "Unknown"));
                stock.setVolatility(toDouble(update.get("volatility")));
                stock.setTrend_bias(toDouble(update.get("trend_bias")));
                stock.setEvent_weight(toDouble(update.get("event_weight")));
                stock.setMomentum(toDouble(update.get("momentum")));
                stock.setListed_at(Instant.now().toString());
                log.info("Auto-registered stock from Kafka: " + ticker);
            }
            stock.setCurrent_price(toDouble(update.get("price")));
            stock.setOpen_price(toDouble(update.get("open")));
            stock.setHigh_price(toDouble(update.get("high")));
            stock.setLow_price(toDouble(update.get("low")));
            stock.setVolume(toLong(update.get("volume")));
            return stock;
        });
    }

    private Double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number n) return n.doubleValue();
        return Double.valueOf(String.valueOf(val));
    }

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        if (val == null) return 0L;
        return Long.valueOf(String.valueOf(val));
    }

    private String stringVal(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return v != null ? String.valueOf(v) : def;
    }
}
