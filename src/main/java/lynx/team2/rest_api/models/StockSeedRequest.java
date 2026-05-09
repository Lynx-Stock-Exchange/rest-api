package lynx.team2.rest_api.models;

public class StockSeedRequest {
    String ticker;
    String name;
    String sector;
    Double start_price;
    Double volatility;
    Double trend_bias;
    Double event_weight;
    Double momentum;

    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public String getSector() { return sector; }
    public Double getStart_price() { return start_price; }
    public Double getVolatility() { return volatility; }
    public Double getTrend_bias() { return trend_bias; }
    public Double getEvent_weight() { return event_weight; }
    public Double getMomentum() { return momentum; }

    public void setTicker(String ticker) { this.ticker = ticker; }
    public void setName(String name) { this.name = name; }
    public void setSector(String sector) { this.sector = sector; }
    public void setStart_price(Double start_price) { this.start_price = start_price; }
    public void setVolatility(Double volatility) { this.volatility = volatility; }
    public void setTrend_bias(Double trend_bias) { this.trend_bias = trend_bias; }
    public void setEvent_weight(Double event_weight) { this.event_weight = event_weight; }
    public void setMomentum(Double momentum) { this.momentum = momentum; }
}
