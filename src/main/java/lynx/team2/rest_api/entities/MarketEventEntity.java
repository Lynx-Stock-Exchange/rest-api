package lynx.team2.rest_api.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "market_events")
public class MarketEventEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "scope", nullable = false)
    private String scope;

    @Column(name = "target")
    private String target;

    @Column(name = "magnitude")
    private Double magnitude;

    @Column(name = "duration_ticks")
    private Integer durationTicks;

    @Column(name = "headline")
    private String headline;

    @Column(name = "triggered_at")
    private Long triggeredAt;

    @Column(name = "triggered_by")
    private String triggeredBy;

    public MarketEventEntity() {}

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getScope() { return scope; }
    public String getTarget() { return target; }
    public Double getMagnitude() { return magnitude; }
    public Integer getDurationTicks() { return durationTicks; }
    public String getHeadline() { return headline; }
    public Long getTriggeredAt() { return triggeredAt; }
    public String getTriggeredBy() { return triggeredBy; }

    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setScope(String scope) { this.scope = scope; }
    public void setTarget(String target) { this.target = target; }
    public void setMagnitude(Double magnitude) { this.magnitude = magnitude; }
    public void setDurationTicks(Integer durationTicks) { this.durationTicks = durationTicks; }
    public void setHeadline(String headline) { this.headline = headline; }
    public void setTriggeredAt(Long triggeredAt) { this.triggeredAt = triggeredAt; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
}
