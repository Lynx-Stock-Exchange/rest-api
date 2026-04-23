package lynx.team2.rest_api.controllers;

import lynx.team2.rest_api.models.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
public class MarketDataController {

    /// TODO: Replace with actual data
    //-- GET /market/status
    @GetMapping("/status")
    public MarketStatus getMarketStatus() {
        return new MarketStatus(
                true,
                "2024-03-15T09:45:00",
                "2024-03-15T10:02:33Z",
                60,
                null
        );
    }

    /// TODO: Replace with actual data
    //-- GET /market/stocks
    @GetMapping("/stocks")
    public List<Stock> getStocks() {
        List<Stock> stocks = new ArrayList<>();
        stocks.add(new Stock(
                "ARKA",
                "Arkadia Technologies",
                "Tech",
                12.34,
                23.45,
                34.56,
                45.67,
                1234,
                0.03,
                1,
                0.25,
                0.12,
                1776941245
        ));
        return stocks;
    }

    /// TODO: Replace with actual data
    //-- GET /market/stocks:ticker
    @GetMapping("/stocks/{ticker}")
    public Stock getStockByTicker(@PathVariable("ticker") String ticker) {
        return new Stock(
                ticker,
                "Arkadia Technologies",
                "Tech",
                12.34,
                23.45,
                34.56,
                45.67,
                1234,
                0.03,
                1,
                0.25,
                0.12,
                1776941245
        );
    }
}
