package lynx.team2.rest_api.models;

import lynx.team2.rest_api.LUtils;

public class Stock {
    private String ticker;          // Unique symbol, e.g. "ARKA". Max 5 characters, uppercase
    private String name;            // Full company name, e.g. "Arkadia Technologies"
    private String sector;          // Sector grouping, e.g. "Tech", "Finance", "Energy"
    private Double current_price;   // Current market price (decimal, 2dp)
    private Double open_price;      // Price at market open for the current simulated day
    private Double high_price;      // Highest price in the current simulated day
    private Double low_price;       // Lowest price in the current simulated day
    private Long volume;            // Total shares traded in current simulated day
    private Double volatility;      // Simulation parameter: price swing magnitude per tick (e.g. 0.03 = 3%)
    private Double trend_bias;      // Simulation parameter: drift per tick (positive = upward, negative = downward)
    private Double event_weight;    // Simulation parameter: multiplier for market event impact on this stock
    private Double momentum;        // Simulation parameter: how strongly order pressure influences price (0.0–1.0)
    private String listed_at;       // ISO 8601 simulated market timestamp

    public Stock() {}

    public Stock(String ticker, String name, String sector, Double current_price, Double open_price, Double high_price, Double low_price, Long volume, Double volatility, Double trend_bias, Double event_weight, Double momentum, String listed_at) {
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
    public Double getCurrent_price() { return current_price; }
    public Double getOpen_price() { return open_price; }
    public Double getHigh_price() { return high_price; }
    public Double getLow_price() { return low_price; }
    public Long getVolume() { return volume; }
    public Double getVolatility() { return volatility; }
    public Double getTrend_bias() { return trend_bias; }
    public Double getEvent_weight() { return event_weight; }
    public Double getMomentum() { return momentum; }
    public String getListed_at() { return listed_at; }

    public void setTicker(String ticker) { this.ticker = ticker; }
    public void setName(String name) { this.name = name; }
    public void setSector(String sector) { this.sector = sector; }
    public void setCurrent_price(Double current_price) { this.current_price = current_price; }
    public void setOpen_price(Double open_price) { this.open_price = open_price; }
    public void setHigh_price(Double high_price) { this.high_price = high_price; }
    public void setLow_price(Double low_price) { this.low_price = low_price; }
    public void setVolume(Long volume) { this.volume = volume; }
    public void setVolatility(Double volatility) { this.volatility = volatility; }
    public void setTrend_bias(Double trend_bias) { this.trend_bias = trend_bias; }
    public void setEvent_weight(Double event_weight) { this.event_weight = event_weight; }
    public void setMomentum(Double momentum) { this.momentum = momentum; }
    public void setListed_at(String listed_at) { this.listed_at = listed_at; }
}
