package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.models.Stock;
import lynx.team2.rest_api.services.MarketDataService;
import lynx.team2.rest_api.services.MarketStateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestClient engineClient;
    private final MarketDataService marketDataService;
    private final MarketStateService marketStateService;
    private volatile double feeRate = 0.001;
    private final String priceSimUrl;
    private final String marketEventsUrl;

    public AdminController(
            KafkaTemplate<String, Object> kafkaTemplate,
            MarketDataService marketDataService,
            MarketStateService marketStateService,
            @Value("${order-book-engine.url:http://order-book-engine:8086}") String engineUrl,
            @Value("${price-simulation.url:http://price-simulation:8080}") String priceSimUrl,
            @Value("${market-events.url:http://market-events:8082}") String marketEventsUrl) {
        this.kafkaTemplate = kafkaTemplate;
        this.marketDataService = marketDataService;
        this.marketStateService = marketStateService;
        this.engineClient = RestClient.builder().baseUrl(engineUrl).build();
        this.priceSimUrl = priceSimUrl;
        this.marketEventsUrl = marketEventsUrl;
    }

    @GetMapping("/platforms")
    public List<String> getPlatforms() {
        return List.of("ARKA");
    }

    @PostMapping("/platforms")
    public Map<String, Object> postPlatform(@RequestBody(required = false) Map<String, Object> payload) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Platform accepted by REST API.");
        response.put("platform", payload == null ? Map.of() : payload);
        return response;
    }

    @DeleteMapping({"/platforms", "/platforms/{platformId}"})
    public Map<String, Object> deletePlatform(@PathVariable(required = false) String platformId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Platform revoked by REST API.");
        response.put("platform_id", platformId);
        return response;
    }

    @GetMapping("/market/status")
    public Map<String, Object> getMarketStatus() {
        return marketStatus();
    }

    @PostMapping("/market/open")
    public Map<String, Object> postOpen() {
        marketStateService.setOpen(true);
        publishAdminCommand(Map.of("action", "OPEN_MARKET"));
        publishMarketStatusEvent(true);
        return marketStatus();
    }

    @PostMapping("/market/close")
    public Map<String, Object> postClose() {
        marketStateService.setOpen(false);
        publishAdminCommand(Map.of("action", "CLOSE_MARKET"));
        publishMarketStatusEvent(false);
        return marketStatus();
    }

    @PutMapping("/market/speed")
    public Map<String, Object> putSpeed(@RequestBody(required = false) Map<String, Object> request) {
        int multiplier = intValue(request, "multiplier", marketStateService.getSpeedMultiplier());
        marketStateService.setSpeedMultiplier(multiplier);
        publishAdminCommand(Map.of("action", "SET_SPEED", "multiplier", multiplier));
        return marketStatus();
    }

    @PostMapping("/stocks")
    public Map<String, Object> postSingleStock(@RequestBody(required = false) Map<String, Object> newStockData) {
        Map<String, Object> stockMap = stockFromPayload(newStockData);
        marketDataService.saveStock(mapToStock(stockMap));
        return stockMap;
    }

    @PostMapping("/stocks/seed")
    public List<Map<String, Object>> postStocks(@RequestBody(required = false) List<Map<String, Object>> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> seeded = new ArrayList<>();
        for (Map<String, Object> stock : stocks) {
            Map<String, Object> stockMap = stockFromPayload(stock);
            marketDataService.saveStock(mapToStock(stockMap));
            seeded.add(stockMap);
        }

        forwardToPriceSim(stocks);

        return seeded;
    }

    private void forwardToPriceSim(List<Map<String, Object>> stocks) {
        if (priceSimUrl == null || priceSimUrl.isBlank()) return;
        try {
            RestClient.builder().baseUrl(priceSimUrl).build()
                    .post()
                    .uri("/api/v1/admin/stocks/seed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(stocks)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.out.println("Warning: could not forward stocks to price-simulation: " + e.getMessage());
        }
    }

    @PostMapping("/options")
    public Map<String, Object> postOption(@RequestBody(required = false) Map<String, Object> payload) {
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("option_id", "opt-" + UUID.randomUUID().toString().substring(0, 8));
        option.put("underlying_ticker", stringValue(payload, "underlying_ticker", "ARKA"));
        option.put("option_type", stringValue(payload, "option_type", "CALL"));
        option.put("strike_price", doubleValue(payload, "strike_price", 100.0));
        option.put("expiry_time", stringValue(payload, "expiry_time", Instant.now().toString()));
        option.put("premium", doubleValue(payload, "initial_premium", 1.0));
        option.put("is_active", true);
        option.put("auto_exercise", true);
        marketDataService.saveOption(option);
        forwardOptionToPriceSim(option);
        return option;
    }

    private void forwardOptionToPriceSim(Map<String, Object> option) {
        if (priceSimUrl == null || priceSimUrl.isBlank()) return;
        try {
            RestClient.builder().baseUrl(priceSimUrl).build()
                    .post()
                    .uri("/api/v1/admin/options")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(option)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.out.println("Warning: could not forward option to price-simulation: " + e.getMessage());
        }
    }

    @PostMapping("/events/trigger")
    public Map<String, Object> postEvent(@RequestBody(required = false) Map<String, Object> payload) {
        Map<String, Object> event = forwardEventToMarketEvents(payload);
        if (event == null) {
            // market-events unreachable — return a structured error so the caller knows
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "market-events service is unavailable. Event was not triggered.");
            return err;
        }
        return event;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> forwardEventToMarketEvents(Map<String, Object> payload) {
        if (marketEventsUrl == null || marketEventsUrl.isBlank()) return null;
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("event_type", stringValue(payload, "event_type", "BULL_RUN"));
            body.put("scope",      stringValue(payload, "scope",      "MARKET"));
            Object target = payload == null ? null : payload.get("target");
            if (target != null) body.put("target", String.valueOf(target));
            body.put("magnitude",     doubleValue(payload, "magnitude",     1.5));
            body.put("duration_ticks", intValue(payload, "duration_ticks", 10));

            return RestClient.builder().baseUrl(marketEventsUrl).build()
                    .post()
                    .uri("/api/v1/market/events/trigger")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            System.out.println("Warning: could not forward event trigger to market-events: " + e.getMessage());
            return null;
        }
    }

    @PostMapping("/events/config")
    public Map<String, Object> postEventsConfig(@RequestBody(required = false) Map<String, Object> payload) {
        forwardEventsConfig(payload);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Event config accepted by REST API.");
        response.put("config", payload == null ? Map.of() : payload);
        return response;
    }

    private void forwardEventsConfig(Map<String, Object> payload) {
        if (marketEventsUrl == null || marketEventsUrl.isBlank() || payload == null) return;
        try {
            RestClient.builder().baseUrl(marketEventsUrl).build()
                    .post()
                    .uri("/api/v1/internal/events/config")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.out.println("Warning: could not forward events config to market-events: " + e.getMessage());
        }
    }

    @PutMapping("/fees")
    public Map<String, Object> putFees(@RequestBody(required = false) Map<String, Object> payload) {
        feeRate = doubleValue(payload, "rate", feeRate);
        publishAdminCommand(Map.of("action", "UPDATE_FEE", "fee_rate", feeRate));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("rate", feeRate);
        response.put("message", "Fee rate updated.");
        return response;
    }

    @GetMapping("/orders")
    public ResponseEntity<String> getOrders(@RequestParam(value = "status", required = false) String status) {
        String resolvedStatus = status == null ? "FILLED" : status;
        try {
            return engineClient.get()
                    .uri("/orders?status={s}", resolvedStatus)
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(502)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("[]");
        }
    }

    private Map<String, Object> marketStatus() {
        String now = Instant.now().toString();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("is_open", marketStateService.isOpen());
        status.put("market_time", now);
        status.put("real_time", now);
        status.put("speed_multiplier", marketStateService.getSpeedMultiplier());
        status.put("active_event", null);
        status.put("fee_rate", feeRate);
        return status;
    }

    private Map<String, Object> stockFromPayload(Map<String, Object> payload) {
        double startPrice = doubleValue(payload, "start_price", 12.34);
        Map<String, Object> stock = new LinkedHashMap<>();
        stock.put("ticker", stringValue(payload, "ticker", "ARKA"));
        stock.put("name", stringValue(payload, "name", "Arkadia Technologies"));
        stock.put("sector", stringValue(payload, "sector", "Tech"));
        stock.put("current_price", startPrice);
        stock.put("open_price", startPrice);
        stock.put("high_price", startPrice);
        stock.put("low_price", startPrice);
        stock.put("volume", 0);
        stock.put("volatility", doubleValue(payload, "volatility", 0.03));
        stock.put("trend_bias", doubleValue(payload, "trend_bias", 1.0));
        stock.put("event_weight", doubleValue(payload, "event_weight", 0.25));
        stock.put("momentum", doubleValue(payload, "momentum", 0.12));
        stock.put("listed_at", Instant.now().toString());
        return stock;
    }

    private Stock mapToStock(Map<String, Object> map) {
        Stock s = new Stock();
        s.setTicker((String) map.get("ticker"));
        s.setName((String) map.get("name"));
        s.setSector((String) map.get("sector"));
        s.setCurrent_price((Double) map.get("current_price"));
        s.setOpen_price((Double) map.get("open_price"));
        s.setHigh_price((Double) map.get("high_price"));
        s.setLow_price((Double) map.get("low_price"));
        s.setVolume(toLong(map.get("volume")));
        s.setVolatility((Double) map.get("volatility"));
        s.setTrend_bias((Double) map.get("trend_bias"));
        s.setEvent_weight((Double) map.get("event_weight"));
        s.setMomentum((Double) map.get("momentum"));
        s.setListed_at((String) map.get("listed_at"));
        return s;
    }

    private Map<String, Object> eventFromPayload(Map<String, Object> payload) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("event_id", "evt-" + UUID.randomUUID().toString().substring(0, 8));
        event.put("event_type", stringValue(payload, "event_type", "SECTOR_SLUMP"));
        event.put("scope", stringValue(payload, "scope", "SECTOR"));
        event.put("target", payload == null ? "Tech" : payload.getOrDefault("target", "Tech"));
        event.put("magnitude", doubleValue(payload, "magnitude", 1.8));
        event.put("duration_ticks", intValue(payload, "duration_ticks", 20));
        event.put("headline", stringValue(payload, "headline", "Manual market event triggered."));
        event.put("triggered_at", Instant.now().toString());
        event.put("triggered_by", "ADMIN");
        return event;
    }

    private void publishAdminCommand(Map<String, Object> payload) {
        kafkaTemplate.send("admin.commands", UUID.randomUUID().toString(), payload);
    }

    private void publishMarketStatusEvent(boolean isOpen) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("event_type", isOpen ? "MARKET_OPEN" : "MARKET_CLOSE");
        event.put("is_open", isOpen);
        event.put("market_time", Instant.now().toString());
        kafkaTemplate.send("market.events", UUID.randomUUID().toString(), event);
    }

    private String stringValue(Map<String, Object> payload, String key, String defaultValue) {
        if (payload == null || payload.get(key) == null) {
            return defaultValue;
        }
        return String.valueOf(payload.get(key));
    }

    private double doubleValue(Map<String, Object> payload, String key, double defaultValue) {
        if (payload == null || payload.get(key) == null) {
            return defaultValue;
        }
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }

    private int intValue(Map<String, Object> payload, String key, int defaultValue) {
        if (payload == null || payload.get(key) == null) {
            return defaultValue;
        }
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        if (val == null) return 0L;
        return Long.valueOf(String.valueOf(val));
    }
}
