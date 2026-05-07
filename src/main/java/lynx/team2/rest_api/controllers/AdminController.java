package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.models.Order;
import lynx.team2.rest_api.models.SpeedUpdateRequest;
import lynx.team2.rest_api.models.StockSeedRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    /**
     * GET /admin/platforms <br>
     * TODO: Replace with actual data
     * @return A list of all registered platforms
     */
    @GetMapping("/platforms")
    public List<String> getPlatforms(@RequestHeader("Authorization") String authHeader) {
        List<String> platforms = new ArrayList<>();
        platforms.add("ARKA");
        return platforms;
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
    public void deletePlatform(@RequestHeader("Authorization") String authHeader) {

    }

    /**
     * POST /admin/market/open <br>
     * Open the market. Starts simulation ticks <br>
     * TODO: Replace with actual function
     */
    @PostMapping("/market/open")
    public void postOpen() {

    }

    /**
     * POST /admin/market/close <br>
     * Close the market. Pending limits: see Section 6.4 <br>
     * TODO: Replace with actual function
     */
    @PostMapping("/market/close")
    public void postClose() {

    }

    /**
     * PUT /admin/market/speed <br>
     * Set time speed multiplier. Body: { multiplier: 60 } <br>
     * TODO: Replace with actual function
     */
    @PutMapping("/market/speed")
    public void putSpeed(
            @RequestBody SpeedUpdateRequest request
    ) {

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
