package lynx.team2.rest_api.models;

import lynx.team2.rest_api.LUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Stock {
    private String ticker;          // Unique symbol, e.g. "ARKA". Max 5 characters, uppercase
    private String name;            // Full company name, e.g. "Arkadia Technologies"
    private String sector;          // Sector grouping, e.g. "Tech", "Finance", "Energy"
    private Double current_price;   // Current market price (decimal, 2dp)
    private Double open_price;      // Price at market open for the current simulated day
    private Double high_price;      // Highest price in the current simulated day
    private Double low_price;       // Lowest price in the current simulated day
    private Integer volume;         // Total shares traded in current simulated day
    private Double volatility;      // Simulation parameter: price swing magnitude per tick (e.g. 0.03 = 3%)
    private Double trend_bias;      // Simulation parameter: drift per tick (positive = upward, negative = downward)
    private Double event_weight;    // Simulation parameter: multiplier for market event impact on this stock
    private Double momentum;        // Simulation parameter: how strongly order pressure influences price (0.0–1.0)

    private long listed_at;         // Simulated market timestamp when the stock was listed


    public Stock(String ticker, String name, String sector, double current_price, double open_price, double high_price, double low_price, int volume, double volatility, double trend_bias, double event_weight, double momentum, long listed_at) {
        this.ticker = ticker;
        this.name = name;
        this.sector = sector;
        this.current_price = current_price;
        this.open_price = open_price;
        this.high_price = high_price;
        this.low_price = low_price;
        this.volume = volume;
        this.volatility = volatility;
        this.trend_bias = trend_bias;
        this.event_weight = event_weight;
        this.momentum = momentum;
        this.listed_at = listed_at;
    }

    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public String getSector() { return sector; }
    public double getCurrent_price() { return current_price; }
    public double getOpen_price() { return open_price; }
    public double getHigh_price() { return high_price; }
    public double getLow_price() { return low_price; }
    public int getVolume() { return volume; }
    public double getVolatility() { return volatility; }
    public double getTrend_bias() { return trend_bias; }
    public double getEvent_weight() { return event_weight; }
    public double getMomentum() { return momentum; }
    public String getListed_at() { return LUtils.longToIsoDate(listed_at); }

    public static Stock getDummy(String ticker) {
        return new Stock(
                ticker,
                "Arkadia Technologies",
                "Tech",
                12.34,
                23.45,
                34.56,
                45.67,
                123,
                0.03,
                1,
                0.25,
                0.12,
                1776941245L
        );
    }
}
