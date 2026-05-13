package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.kafka.KafkaProducerService;
import lynx.team2.rest_api.models.*;
import lynx.team2.rest_api.state.StateStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin("*")
public class AdminController {

    private final StateStore stateStore;
    private final KafkaProducerService kafkaProducerService;
    private final RestClient restClient = RestClient.create();

    @Value("${market.events.url}")
    private String marketEventsUrl;

    public AdminController(StateStore stateStore, KafkaProducerService kafkaProducerService) {
        this.stateStore = stateStore;
        this.kafkaProducerService = kafkaProducerService;
    }

    // PLATFORMS

    @PostMapping("/platforms")
    public ResponseEntity<Map<String, Object>> postPlatform(@RequestBody PlatformCreateRequest req) {
        String id = UUID.randomUUID().toString();
        String apiKey = UUID.randomUUID().toString().replace("-", "");
        String apiSecret = UUID.randomUUID().toString().replace("-", "");

        // Store credentials locally so PlatformService can verify them without admin panel
        stateStore.registerPlatformCredentials(apiKey, apiSecret, id, req.getName());

        // Broadcast to all other services via Kafka
        Map<String, Object> kafkaPayload = new HashMap<>();
        kafkaPayload.put("id", id);
        kafkaPayload.put("name", req.getName());
        kafkaPayload.put("api_key", apiKey);
        kafkaProducerService.sendAdminCommand("PLATFORM_ADDED", kafkaPayload);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", req.getName());
        result.put("api_key", apiKey);
        result.put("api_secret", apiSecret);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/platforms/{id}")
    public ResponseEntity<Void> deletePlatform(@PathVariable String id) {
        stateStore.removeLocalPlatformById(id);
        kafkaProducerService.sendAdminCommand("PLATFORM_REMOVED", Map.of("id", id));
        return ResponseEntity.noContent().build();
    }

    // MARKET

    @GetMapping("/market/status")
    public ResponseEntity<Map<String, Object>> getMarketStatus() {
        MarketStatus status = stateStore.getMarketStatus();
        Map<String, Object> res = new HashMap<>();
        res.put("is_open", status.isIs_open());
        res.put("market_time", status.getMarket_time());
        res.put("real_time", status.getReal_time());
        res.put("speed_multiplier", status.getSpeed_multiplier());
        res.put("active_event", status.getActive_event_id());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/market/open")
    public ResponseEntity<Void> postOpen() {
        kafkaProducerService.sendAdminCommand("OPEN_MARKET", Map.of());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/market/close")
    public ResponseEntity<Void> postClose() {
        kafkaProducerService.sendAdminCommand("CLOSE_MARKET", Map.of());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PutMapping("/market/speed")
    public ResponseEntity<Void> putSpeed(@RequestBody SpeedUpdateRequest request) {
        kafkaProducerService.sendAdminCommand("MARKET_SPEED_UPDATE", Map.of("multiplier", request.getMultiplier()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    // STOCKS

    @GetMapping("/stocks")
    public ResponseEntity<List<Stock>> getAllStocks() {
        return ResponseEntity.ok(List.copyOf(stateStore.getAllStocks()));
    }

    @GetMapping("/stocks/{ticker}")
    public ResponseEntity<Stock> getStock(@PathVariable String ticker) {
        return stateStore.getStock(ticker)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/stocks")
    public ResponseEntity<Void> postSingleStock(@RequestBody StockSeedRequest req) {
        kafkaProducerService.sendAdminCommand("STOCK_ADDED", buildStockPayload(req));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/stocks/seed")
    public ResponseEntity<Void> seedStocks(@RequestBody List<StockSeedRequest> stocks) {
        for (StockSeedRequest req : stocks) {
            kafkaProducerService.sendAdminCommand("STOCK_ADDED", buildStockPayload(req));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    private Map<String, Object> buildStockPayload(StockSeedRequest req) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ticker", req.getTicker());
        payload.put("name", req.getName());
        payload.put("sector", req.getSector());
        payload.put("start_price", req.getStart_price());
        payload.put("volatility", req.getVolatility());
        payload.put("trend_bias", req.getTrend_bias());
        payload.put("event_weight", req.getEvent_weight());
        payload.put("momentum", req.getMomentum());
        return payload;
    }

    @PutMapping("/stocks/{ticker}")
    public ResponseEntity<Void> updateStock(@PathVariable String ticker, @RequestBody StockUpdateRequest req) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ticker", ticker);
        if (req.getName() != null) payload.put("name", req.getName());
        if (req.getSector() != null) payload.put("sector", req.getSector());
        if (req.getVolatility() != null) payload.put("volatility", req.getVolatility());
        if (req.getTrend_bias() != null) payload.put("trend_bias", req.getTrend_bias());
        if (req.getEvent_weight() != null) payload.put("event_weight", req.getEvent_weight());
        if (req.getMomentum() != null) payload.put("momentum", req.getMomentum());
        kafkaProducerService.sendAdminCommand("STOCK_UPDATED", payload);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/stocks/{ticker}")
    public ResponseEntity<Void> deleteStock(@PathVariable String ticker) {
        kafkaProducerService.sendAdminCommand("STOCK_REMOVED", Map.of("ticker", ticker));
        return ResponseEntity.noContent().build();
    }

    // OPTIONS

    @GetMapping("/options")
    public ResponseEntity<List<OptionContract>> getAllOptions() {
        return ResponseEntity.ok(List.copyOf(stateStore.getAllOptions()));
    }

    @PostMapping("/options")
    public ResponseEntity<Void> postOption(@RequestBody OptionCreateAdminRequest req) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("underlying_ticker", req.getUnderlying_ticker());
        payload.put("option_type", req.getOption_type());
        payload.put("strike_price", req.getStrike_price());
        payload.put("expiry_time", req.getExpiry_time());
        payload.put("initial_premium", req.getInitial_premium());
        kafkaProducerService.sendAdminCommand("OPTION_ADDED", payload);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    // EVENTS

    @GetMapping("/events")
    public ResponseEntity<List<MarketEvent>> getEvents() {
        return ResponseEntity.ok(stateStore.getRecentEvents());
    }

    @PostMapping("/events/trigger")
    public ResponseEntity<Void> postEvent(@RequestBody MarketEventTriggerRequest req) {
        Map<String, Object> forwardBody = new HashMap<>();
        forwardBody.put("event_type", req.getEvent_type());
        forwardBody.put("scope", req.getScope());
        forwardBody.put("target", req.getTarget());
        forwardBody.put("magnitude", req.getMagnitude());
        forwardBody.put("duration_ticks", req.getDuration_ticks());

        try {
            restClient.post()
                    .uri(marketEventsUrl + "/api/v1/market/events/trigger")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(forwardBody)
                    .retrieve()
                    .toBodilessEntity();
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    // FEES

    @GetMapping("/fees")
    public ResponseEntity<Map<String, Object>> getFees() {
        return ResponseEntity.ok(Map.of("fee_rate", stateStore.getFeeRate()));
    }

    @PutMapping("/fees")
    public ResponseEntity<Void> putFees(@RequestBody Map<String, Object> feeUpdate) {
        if (feeUpdate.containsKey("fee_rate")) {
            double rate = ((Number) feeUpdate.get("fee_rate")).doubleValue();
            kafkaProducerService.sendAdminCommand("FEE_UPDATED", Map.of("fee_rate", rate));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    // REVENUE

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenue() {
        return ResponseEntity.ok(Map.of("total_revenue", stateStore.getTotalRevenue()));
    }
}