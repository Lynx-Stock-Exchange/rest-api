package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.models.PortfolioPosition;
import lynx.team2.rest_api.models.Trade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

    /**
     * GET /portfolio/:platform_user_id <br>
     * TODO: Replace with actual data
     * @return All current positions for a user as {@link PortfolioPosition}. Positions are derived from exchange trade history and reflect only assets acquired through the exchange
     */
    @GetMapping("/{platform_user_id}")
    public PortfolioPosition getPortfolioById(@PathVariable("platform_user_id") String platform_user_id) {
        return PortfolioPosition.getDummy(platform_user_id);
    }

    /**
     * GET /portfolio/:platform_user_id/trades <br>
     * TODO: Replace with actual data
     * @return The full trade history for a user as a list of {@link Trade}. Includes all executed fills with fees
     */
    @GetMapping("/{platform_user_id}/trades")
    public List<Trade> getTradesById(@PathVariable("platform_user_id") String platform_user_id) {
        List<Trade> trades = new ArrayList<>();
        trades.add(Trade.getDummy("ARKA"));
        return trades;
    }

}