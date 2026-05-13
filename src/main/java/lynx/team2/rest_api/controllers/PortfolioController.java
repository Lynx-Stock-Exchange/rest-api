package lynx.team2.rest_api.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lynx.team2.rest_api.internal.Platform;
import lynx.team2.rest_api.models.PortfolioPosition;
import lynx.team2.rest_api.models.Trade;
import lynx.team2.rest_api.state.StateStore;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

    private final StateStore stateStore;

    public PortfolioController(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    @GetMapping("")
    public List<PortfolioPosition> getPortfolio(
            @RequestParam("platform_user_id") String platformUserId,
            HttpServletRequest request) {

        Platform platform = (Platform) request.getAttribute("platform");
        List<Trade> trades = stateStore.getTradesByPlatformAndUser(platform.getId(), platformUserId);

        // Calculate positions from trades
        Map<String, Integer> stockQty = new HashMap<>();
        Map<String, Double> stockCost = new HashMap<>();
        Map<String, String> stockType = new HashMap<>();

        for (Trade t : trades) {
            String ticker = t.getInstrument_id();
            int qty = t.getQuantity();
            double price = t.getPrice();
            stockType.put(ticker, t.getInstrument_type());

            if ("BUY".equals(t.getSide())) {
                stockQty.merge(ticker, qty, Integer::sum);
                stockCost.merge(ticker, qty * price, Double::sum);
            } else if ("SELL".equals(t.getSide())) {
                stockQty.merge(ticker, -qty, Integer::sum);
                stockCost.merge(ticker, -qty * price, Double::sum);
            }
        }

        List<PortfolioPosition> result = new ArrayList<>();
        stockQty.forEach((ticker, qty) -> {
            if (qty != 0) {
                double totalCost = stockCost.getOrDefault(ticker, 0.0);
                double avgPrice = qty > 0 ? totalCost / qty : 0.0;

                result.add(new PortfolioPosition(
                        platform.getId(),
                        platformUserId,
                        stockType.getOrDefault(ticker, "STOCK"),
                        ticker,
                        qty,
                        avgPrice
                ));
            }
        });

        return result;
    }

    @GetMapping("/trades")
    public List<Trade> getTrades(
            @RequestParam("platform_user_id") String platformUserId,
            HttpServletRequest request) {

        Platform platform = (Platform) request.getAttribute("platform");
        return stateStore.getTradesByPlatformAndUser(platform.getId(), platformUserId);
    }
}
