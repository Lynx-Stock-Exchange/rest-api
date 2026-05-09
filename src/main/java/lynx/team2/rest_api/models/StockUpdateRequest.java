package lynx.team2.rest_api.models;

public class StockUpdateRequest {
    private String name;
    private String sector;
    private Double volatility;
    private Double trend_bias;
    private Double event_weight;
    private Double momentum;

    public String getName() { return name; }
    public String getSector() { return sector; }
    public Double getVolatility() { return volatility; }
    public Double getTrend_bias() { return trend_bias; }
    public Double getEvent_weight() { return event_weight; }
    public Double getMomentum() { return momentum; }

    public void setName(String name) { this.name = name; }
    public void setSector(String sector) { this.sector = sector; }
    public void setVolatility(Double volatility) { this.volatility = volatility; }
    public void setTrend_bias(Double trend_bias) { this.trend_bias = trend_bias; }
    public void setEvent_weight(Double event_weight) { this.event_weight = event_weight; }
    public void setMomentum(Double momentum) { this.momentum = momentum; }
}