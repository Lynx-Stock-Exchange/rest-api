package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.models.Order;
import lynx.team2.rest_api.models.SpeedUpdateRequest;
import lynx.team2.rest_api.models.StockSeedRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin("*") // remove this when deployed
public class AdminController {

    @Value("${kafka.topics.admin-commands}")
    private String adminCommandsTopic;

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

    /**
     * GET /admin/platforms <br>
     * TODO: Replace with actual data
     * @return A list of all registered platforms
     */
    @GetMapping("/platforms")
    public List<String> getPlatforms() {
        //List<String> platforms = new ArrayList<>();
        //platforms.add("ARKA");
        return null;
    }

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

    /**
     * POST /admin/market/open <br>
     * Open the market. Starts simulation ticks <br>
     * TODO: Replace with actual function
     */
    @PostMapping("/market/open")
    public ResponseEntity<Void> postOpen() {
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
        System.out.println("MARKET SPEED UPDATE TO " + request.getMultiplier());
        publish("MARKET_SPEED_UPDATE", Map.of("multiplier", request.getMultiplier()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * POST /admin/stocks <br>
     * Add a new stock. Takes full stock seed object <br>
     * TODO: Replace with actual function
     */
    @PostMapping("/stocks")
    public void postSingleStock(
            @RequestBody StockSeedRequest newStockData
    ) {

    }

    /**
     * POST /admin/stocks/seed <br>
     * Bulk seed stocks from JSON. See Section 9 <br>
     * TODO: Replace with actual function
     */
    @PostMapping("/stocks/seed")
    public void postStocks() {

    }

    /**
     * POST /admin/options <br>
     * Add a new option contract <br>
     * TODO: Replace with actual function
     */
    @PostMapping("/options")
    public void postOption() {

    }

    /**
     * POST /admin/events/trigger <br>
     * Manually trigger a market event. Body: event object <br>
     * TODO: Replace with actual function
     */
    @PostMapping("/events/trigger")
    public void postEvent() {

    }

    /**
     * Put /admin/fees <br>
     * Update exchange fee rate. Body: { rate: 0.001 } <br>
     * TODO: Replace with actual function
     */
    @PutMapping("/fees")
    public void putFees() {

    }
}
