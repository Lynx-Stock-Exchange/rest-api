package lynx.team2.rest_api.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "ohlc_history")
public class OHLCPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ticker", nullable = false, length = 5)
    private String ticker;

    @Column(name = "interval_type")
    private String intervalType;

    @Column(name = "close_price")
    private Double closePrice;

    @Column(name = "open_price")
    private Double openPrice;

    @Column(name = "high_price")
    private Double highPrice;

    @Column(name = "low_price")
    private Double lowPrice;

    @Column(name = "volume")
    private Integer volume;

    @Column(name = "timestamp")
    private Long timestamp;

    public OHLCPointEntity() {}

    public Long getId() { return id; }
    public String getTicker() { return ticker; }
    public String getIntervalType() { return intervalType; }
    public Double getClosePrice() { return closePrice; }
    public Double getOpenPrice() { return openPrice; }
    public Double getHighPrice() { return highPrice; }
    public Double getLowPrice() { return lowPrice; }
    public Integer getVolume() { return volume; }
    public Long getTimestamp() { return timestamp; }

    public void setTicker(String ticker) { this.ticker = ticker; }
    public void setIntervalType(String intervalType) { this.intervalType = intervalType; }
    public void setClosePrice(Double closePrice) { this.closePrice = closePrice; }
    public void setOpenPrice(Double openPrice) { this.openPrice = openPrice; }
    public void setHighPrice(Double highPrice) { this.highPrice = highPrice; }
    public void setLowPrice(Double lowPrice) { this.lowPrice = lowPrice; }
    public void setVolume(Integer volume) { this.volume = volume; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
