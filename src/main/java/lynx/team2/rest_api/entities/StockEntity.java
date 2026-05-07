package lynx.team2.rest_api.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "stocks")
public class StockEntity {

    @Id
    @Column(name = "ticker", length = 5)
    private String ticker;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sector")
    private String sector;

    @Column(name = "current_price")
    private Double currentPrice;

    @Column(name = "open_price")
    private Double openPrice;

    @Column(name = "high_price")
    private Double highPrice;

    @Column(name = "low_price")
    private Double lowPrice;

    @Column(name = "volume")
    private Integer volume;

    @Column(name = "volatility")
    private Double volatility;

    @Column(name = "trend_bias")
    private Double trendBias;

    @Column(name = "event_weight")
    private Double eventWeight;

    @Column(name = "momentum")
    private Double momentum;

    @Column(name = "listed_at")
    private Long listedAt;

    public StockEntity() {}

    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public String getSector() { return sector; }
    public Double getCurrentPrice() { return currentPrice; }
    public Double getOpenPrice() { return openPrice; }
    public Double getHighPrice() { return highPrice; }
    public Double getLowPrice() { return lowPrice; }
    public Integer getVolume() { return volume; }
    public Double getVolatility() { return volatility; }
    public Double getTrendBias() { return trendBias; }
    public Double getEventWeight() { return eventWeight; }
    public Double getMomentum() { return momentum; }
    public Long getListedAt() { return listedAt; }

    public void setTicker(String ticker) { this.ticker = ticker; }
    public void setName(String name) { this.name = name; }
    public void setSector(String sector) { this.sector = sector; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
    public void setOpenPrice(Double openPrice) { this.openPrice = openPrice; }
    public void setHighPrice(Double highPrice) { this.highPrice = highPrice; }
    public void setLowPrice(Double lowPrice) { this.lowPrice = lowPrice; }
    public void setVolume(Integer volume) { this.volume = volume; }
    public void setVolatility(Double volatility) { this.volatility = volatility; }
    public void setTrendBias(Double trendBias) { this.trendBias = trendBias; }
    public void setEventWeight(Double eventWeight) { this.eventWeight = eventWeight; }
    public void setMomentum(Double momentum) { this.momentum = momentum; }
    public void setListedAt(Long listedAt) { this.listedAt = listedAt; }
}
