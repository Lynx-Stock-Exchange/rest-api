package lynx.team2.rest_api.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "market_state")
public class MarketStateEntity {

    // Singleton row — always id = 1
    @Id
    @Column(name = "id")
    private Integer id = 1;

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen = false;

    @Column(name = "market_time")
    private Long marketTime;

    @Column(name = "real_time")
    private Long realTime;

    @Column(name = "speed_multiplier")
    private Integer speedMultiplier = 1;

    @Column(name = "active_event_id")
    private String activeEventId;

    public MarketStateEntity() {}

    public Integer getId() { return id; }
    public Boolean getIsOpen() { return isOpen; }
    public Long getMarketTime() { return marketTime; }
    public Long getRealTime() { return realTime; }
    public Integer getSpeedMultiplier() { return speedMultiplier; }
    public String getActiveEventId() { return activeEventId; }

    public void setId(Integer id) { this.id = id; }
    public void setIsOpen(Boolean isOpen) { this.isOpen = isOpen; }
    public void setMarketTime(Long marketTime) { this.marketTime = marketTime; }
    public void setRealTime(Long realTime) { this.realTime = realTime; }
    public void setSpeedMultiplier(Integer speedMultiplier) { this.speedMultiplier = speedMultiplier; }
    public void setActiveEventId(String activeEventId) { this.activeEventId = activeEventId; }
}
