package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.models.OptionContract;
import lynx.team2.rest_api.models.SpeedUpdateRequest;
import lynx.team2.rest_api.models.Stock;
import lynx.team2.rest_api.models.StockSeedRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin("*") // remove this when deployed
public class AdminController {

    @Value("${kafka.topics.admin-commands}")
    private String adminCommandsTopic;

    private boolean marketOpen = false;
    private int speedMultiplier = 1;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AdminController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private void publish(String action, Map<String, Object> payload) {
        kafkaTemplate.send(adminCommandsTopic, new AdminCommand(action, payload));
    }

    private static class AdminCommand {
        private final String action;
        private final Map<String, Object> payload;

        AdminCommand(String action, Map<String, Object> payload) {
            this.action = action;
            this.payload = payload;
        }

        public String getAction() { return action; }
        public Map<String, Object> getPayload() { return payload; }
    }


    // -------------------------------------------------------------------------
    // PLATFORMS
    // -------------------------------------------------------------------------

    /**
     * POST /admin/platforms <br>
     * Register a new broker platform <br>
     * TODO: Replace with actual function
     * @return The API key + secret
     */
    @PostMapping("/platforms")
    public String postPlatform() {
        return null;
    }

    /**
     * DELETE /admin/platforms <br>
     * Revoke a platform's access. All orders cancelled <br>
     * TODO: Replace with actual function
     */
    @DeleteMapping("/platforms")
    public void deletePlatform() {

    }



    // -------------------------------------------------------------------------
    // MARKET
    // -------------------------------------------------------------------------

    /**
     * GET /admin/market/status <br>
     * Get current market state: open/closed, simulated time, speed, active event <br>
     * TODO: Fetch live market state from the simulation engine service
     */
    @GetMapping("/market/status")
    public ResponseEntity<Map<String, Object>> getMarketStatus() {
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("is_open", marketOpen);
        status.put("market_time", "2024-03-15T09:45:00");
        status.put("real_time", "2024-03-15T10:02:33Z");
        status.put("speed_multiplier", speedMultiplier);
        status.put("active_event", null);
        return ResponseEntity.ok(status);
    }

    /**
     * POST /admin/market/open <br>
     * Open the market. Starts simulation ticks <br>
     * TODO: Replace with actual function
     */
    @PostMapping("/market/open")
    public ResponseEntity<Void> postOpen() {
        marketOpen = true;
        System.out.println("MARKET OPEN");
        publish("OPEN_MARKET", Map.of());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * POST /admin/market/close <br>
     * Close the market. Pending limits: see Section 6.4 <br>
     * TODO: Replace with actual function
     */
    @PostMapping("/market/close")
    public ResponseEntity<Void> postClose() {
        marketOpen = false;
        System.out.println("MARKET CLOSE");
        publish("CLOSE_MARKET", Map.of());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * PUT /admin/market/speed <br>
     * Set time speed multiplier. Body: { multiplier: 60 } <br>
     * TODO: Replace with actual function
     */
    @PutMapping("/market/speed")
    public ResponseEntity<Void> putSpeed(@RequestBody SpeedUpdateRequest request){
        speedMultiplier = request.getMultiplier();
        publish("MARKET_SPEED_UPDATE", Map.of("multiplier", speedMultiplier));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }


    // -------------------------------------------------------------------------
    // STOCKS
    // -------------------------------------------------------------------------

    /**
     * GET /admin/stocks <br>
     * List all listed stocks <br>
     * TODO: Fetch all stocks from the stock registry service
     */
    @GetMapping("/stocks")
    public ResponseEntity<List<Stock>> getAllStocks() {
        return ResponseEntity.ok(List.of(Stock.getDummy("ARKA"), Stock.getDummy("LYNX")));
    }

    /**
     * GET /admin/stocks/{ticker} <br>
     * Get a single stock by ticker <br>
     * TODO: Fetch stock by ticker from the stock registry service; return 404 if not found
     */
    @GetMapping("/stocks/{ticker}")
    public ResponseEntity<Stock> getStock(@PathVariable String ticker) {
        return ResponseEntity.ok(Stock.getDummy(ticker));
    }

    /**
     * POST /admin/stocks <br>
     * Add a new stock. Takes full stock seed object <br>
     * TODO: Validate ticker uniqueness, persist to stock registry, publish STOCK_ADDED to Kafka
     */
    @PostMapping("/stocks")
    public ResponseEntity<Stock> postSingleStock(@RequestBody StockSeedRequest newStockData) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Stock.getDummy("ARKA"));
    }

    /**
     * POST /admin/stocks/seed <br>
     * Bulk seed stocks from JSON. See Section 9 <br>
     * TODO: Validate each entry, deduplicate tickers, persist all, publish STOCKS_SEEDED to Kafka
     */
    @PostMapping("/stocks/seed")
    public ResponseEntity<List<Stock>> seedStocks(@RequestBody List<StockSeedRequest> stocks) {
        return ResponseEntity.status(HttpStatus.CREATED).body(List.of(Stock.getDummy("ARKA"), Stock.getDummy("LYNX")));
    }


    // -------------------------------------------------------------------------
    // OPTIONS
    // -------------------------------------------------------------------------

    /**
     * GET /admin/options <br>
     * List all option contracts <br>
     * TODO: Fetch all option contracts from the options registry service
     */
    @GetMapping("/options")
    public ResponseEntity<List<OptionContract>> getAllOptions() {
        return ResponseEntity.ok(List.of(OptionContract.getDummy("OPT-001"), OptionContract.getDummy("OPT-002")));
    }

    /**
     * GET /admin/options/{optionId} <br>
     * Get a single option contract by ID <br>
     * TODO: Fetch option by ID from the options registry service; return 404 if not found
     */
    @GetMapping("/options/{optionId}")
    public ResponseEntity<OptionContract> getOption(@PathVariable String optionId) {
        return ResponseEntity.ok(OptionContract.getDummy(optionId));
    }

    /**
     * POST /admin/options <br>
     * Add a new option contract <br>
     * TODO: Validate underlying ticker exists, generate option_id, persist, publish OPTION_ADDED to Kafka
     */
    @PostMapping("/options")
    public ResponseEntity<OptionContract> postOption(@RequestBody Map<String, Object> optionData) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OptionContract.getDummy("OPT-001"));
    }


    // -------------------------------------------------------------------------
    // EVENTS
    // -------------------------------------------------------------------------

    /**
     * POST /admin/events/trigger <br>
     * Manually trigger a market event. Body: event object <br>
     * TODO: Validate event type and payload, publish MARKET_EVENT to Kafka
     */
    @PostMapping("/events/trigger")
    public ResponseEntity<Void> postEvent(@RequestBody Map<String, Object> event) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }


    // -------------------------------------------------------------------------
    // FEES
    // -------------------------------------------------------------------------

    /**
     * GET /admin/fees <br>
     * Get the current exchange fee configuration <br>
     * TODO: Fetch current fee config from the fee service or config store
     */
    @GetMapping("/fees")
    public ResponseEntity<Map<String, Object>> getFees() {
        return ResponseEntity.ok(Map.of(
                "fee_rate", 0.001
        ));
    }

    /**
     * PUT /admin/fees <br>
     * Update exchange fee rates. Body: { trade_fee_rate, option_fee_rate, min_fee } <br>
     * TODO: Validate rates are non-negative, persist to config store, publish FEE_UPDATED to Kafka
     */
    @PutMapping("/fees")
    public ResponseEntity<Map<String, Object>> putFees(@RequestBody Map<String, Object> feeUpdate) {
        return ResponseEntity.ok(Map.of(
                "fee_rate", 0.001
        ));
    }

    /**
     * GET /admin/revenue <br>
     * Get total exchange revenue collected from fees <br>
     * TODO: Aggregate fee revenue from the trade ledger service; optionally filter by date range via query params
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenue() {
        return ResponseEntity.ok(Map.of(
                "total_revenue", 12345.67
        ));
    }
}
